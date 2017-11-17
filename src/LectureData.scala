import scala.Tuple2;
import scala.collection.mutable.HashMap
import scala.io.Source
import scala.util.control.Breaks
import scala.collection.mutable.MutableList
import main.GraphSpark
import main.GraphSpark
import org.apache.spark.graphx.Edge
import org.apache.spark.graphx.VertexId
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.Graph
import org.apache.spark.SparkConf
import org.apache.spark.graphx.EdgeDirection


class LectureData(nom_fichier : String) {
  val conf = new SparkConf().setAppName("SparkMe Application").setMaster("local")
   val sc = new SparkContext(conf)
  
  var correspondance:HashMap[String,Integer]=new HashMap[String,Integer]();
  var cont=Source.fromFile(nom_fichier).getLines
  val loop = new Breaks;
    loop.breakable {
      for(line<-cont){
        var line2=line.replace("	", "|");
        var elements=line2.split('|')
        for(i<-0 until elements.size){
          correspondance+=elements(i)->i
        }
        loop.break;
      }
    }
  
  //Fonction permettant de changer la colonne correspondant au label
  def ChangeLabel(label:String){
    var buff=correspondance(label)
    correspondance.update(label, 2)
    for(i<-correspondance){
      if(i._2==2 && !i._1.equals(label))
        correspondance.update(i._1,buff)
    }
  }
  
  //Correspond à toute les colonnes à supprimer
  var selectRow:HashMap[String,Integer]=new HashMap[String,Integer]();
  
  //Rempli la variable selectRow selon les argument en entrée
  def RowSelection(rowList : Array[String]):HashMap[String,Integer]={
    var buffRow=correspondance;
    for(row:String<-rowList){
      //println(correspondance(row))
      var value:Int=correspondance(row);
      buffRow=buffRow.-(row);
      println(row);
    }
    for(i<-correspondance){
      if(i._2<=2){
        buffRow=buffRow.-(i._1);
      }
    }
    this.selectRow=buffRow;
    return buffRow;
  }
  
  def Parsing(entry_to_read : Int):Array[Array[String]]={
    var content=Source.fromFile(nom_fichier).getLines
    var data: Array[Array[String]]=new Array[Array[String]](entry_to_read);
    
    var cmpt=0;
    val loop = new Breaks;
    loop.breakable {
      for (line:String <- content) {
        var simpleList:Array[String]=new Array[String](correspondance.size);
        var line2:String=line.replace("; ", ";");
        line2=line.replace("	", "|");
        var elements=line2.split('|');
        while(elements.size<correspondance.size){
          elements:+="";
        }
        
        for(i<-correspondance)
          simpleList(i._2)=elements(i._2)
        
        var simpleList1:Array[String]=simpleList;
        for((i,j)<-selectRow){
          simpleList1(j)=""
        }
        
        data(cmpt)=simpleList1;
        cmpt+=1;
        if(cmpt%100000==0){
          println(cmpt)
        } else if(cmpt>=entry_to_read){loop.break}
      }
    }
    return data;
  }
  
def RunGraph(data : Array[Array[String]],correspondance:HashMap[String,Integer]): (Array[(VertexId, (String, String))],Array[Edge[Int]])={
    val graph:GraphSpark=new GraphSpark();
    var (nodes,edges)=graph.BuildGraph(data,correspondance)
    
    for(i<-0 until nodes.length){
      //println(nodes(i))
    }
    for(i<-0 until edges.length){
      //println(edges(i))
    }
    println("Done")
    println("nodesNbr : "+nodes.length)
    println("edgesNbr : "+edges.length)
    
    return(nodes,edges)
    
  }
  
  def searchProteine(protein:String,nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]])={
    val vertices: RDD[(VertexId, (String, String))] = sc.parallelize(nodes)
    val relationships: RDD[Edge[Int]] = sc.parallelize(edges)
    
    var graphes=Graph(vertices,relationships);
    var proteine:VertexId=0;
    
    for(i<-nodes){
      if(i._2._1.equals(protein))
        proteine=i._1;
    }
    
    val graph:GraphSpark=new GraphSpark();
    //**A VERIFIER**
    println(graphes.collectNeighbors(EdgeDirection.Either).id)

    var nod=graphes.collectNeighbors(EdgeDirection.Either).lookup(proteine)(0);
    
    
    var target:(VertexId,(String,String))=(1,("t","r"))
    for(i<-graphes.vertices.collect()){
      if(i._1==proteine){
        target=i;
      }
    }
    
    var test:Array[(VertexId, (String, String))]=Array((1,(target._2._1,target._2._2)));
    nod=test++nod;
    
    var edg:Array[Edge[Int]]=Array[Edge[Int]]();
    var cmpt=1;
    for(i<-graphes.edges.collect()){
      if(i.dstId.equals(proteine)){
        edg=edg++Array(Edge(cmpt,0,i.attr));
        cmpt+=1;
      } else if(i.srcId.equals(proteine)){
        edg=edg++Array(Edge(0,cmpt,i.attr));
        cmpt+=1;
      }
      
    }
    
    //graphes.collectNeighbors(EdgeDirection.Either).lookup(proteine)(0).foreach(println)

    graph.MakeJson(nod, edg)
    //graphes.collectEdges(EdgeDirection.Either).lookup(proteine)
    
    
  }
  
  def propagationLabel(protein_name:String,nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]]): (Array[(VertexId, (String, String))],Array[Edge[Int]])={
    val vertices: RDD[(VertexId, (String, String))] = sc.parallelize(nodes)
    val relationships: RDD[Edge[Int]] = sc.parallelize(edges)
    
    var graphes=Graph(vertices,relationships);
    var proteine:VertexId=0;
    
    var prot:(VertexId, (String, String))=(0,("b","b"));
    var cmptNodes=0;
    for(i<-0 until nodes.size){
      if(nodes(i)._2._1.equals(protein_name)){
        proteine=nodes(i)._1;
        prot=nodes(i)
        cmptNodes=i;
      }
    }
    
    var nod=graphes.collectNeighbors(EdgeDirection.Either).lookup(proteine)(0);
    
    if(nod.size==0){
      return(nodes,edges)
    }
    var edg=graphes.collectEdges(EdgeDirection.Either).lookup(proteine)(0);
    
    var listeLabel :HashMap[String,Int]=new HashMap[String,Int]();
    var cmpt=0;
    for(i<-edg){
      if(i.srcId!=proteine){
        listeLabel.+=(graphes.vertices.lookup(i.srcId)(0)._2->i.attr);
      } else{
        listeLabel.+=(graphes.vertices.lookup(i.dstId)(0)._2->i.attr);
      }
    }
    
    var listeAttribut:HashMap[String,Int]=new HashMap[String,Int]();
    for(i<-listeLabel){
      if(i!=null){
        var elements=i._1.split(";");
        for(j:String<-elements){
          if(!listeAttribut.contains(j)){
            listeAttribut.+=(j->i._2);
          } else{
            listeAttribut.update(j, listeAttribut(j)+i._2)
          }
        }
      }
    }
    
    var max=0;
    var lab=new MutableList[String]();
    for(i<-listeAttribut){
      if(i._2>max && !i._1.equals("")){
        max=i._2;
        lab.clear()
        lab+=(i._1);
      } else if (i._2==max && !i._1.equals("")){
        lab+=(i._1);
      }
    }
    
    var labe="";
    if(lab.size>=1){
      labe=lab(0)
    }
    
    for(i<-1 until lab.size){
      labe=labe+";"+lab(i)
    }
    nodes.update(cmptNodes, (prot._1, (prot._2._1,labe)))
    println("id:"+prot._2._1+" ancien : "+prot._2._2+" ; nouveau : "+labe);
    //Thread.sleep(500);
    return(nodes,edges)
  }
  
  def makeJson(nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]])={
    val graph:GraphSpark=new GraphSpark();
    graph.MakeJson(nodes, edges)
  }
  
  
}