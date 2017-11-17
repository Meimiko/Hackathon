import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import scala.collection.mutable.MutableList
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import scala.Iterator
import main.GraphSpark
import scala.collection.mutable.HashMap

object App2 {
  val conf = new SparkConf().setAppName("SparkMe Application").setMaster("local")
  val sc = new SparkContext(conf)
  
  def BuildGraph(tableau : Array[Array[String]],correspondance:HashMap[String,Integer]): (Array[(VertexId, (String, String))],Array[Edge[Int]]) = {
    var nbrItem=tableau.length-1
    var nbrCol=tableau(0).length
    
    var nodes=new Array[(VertexId, (String, String))](nbrItem);
    
    var label=""
    var colLabel=2;
    for(i<-correspondance){
      if(i._2==2){
        label=i._1
      }
    }
    for(i<-0 until tableau(0).size){
      if(tableau(0)(i).equals(label))
          colLabel=i
    }
    
    //Creation de tout les noeuds
    var cmptId:VertexId=0;
    for(i<- 0 until nbrItem){
      nodes(i)=(cmptId, (tableau(i+1)(0), tableau(i+1)(colLabel)));
      cmptId+=1;
    }

    var edge=new HashMap[(Int,Int),Int]();
    for(k<- correspondance){
      if(k._2>3){
        var attributs=new MutableList[String]();
        //récupère tout les valeurs possible pour l'attribut étudié
        for(j<- 0 until nbrItem){
          var contentJ=tableau(j+1)(k._2);
            var elementsJ=contentJ.split(";");
            for(i<-elementsJ){
              if(!i.equals("") && !attributs.contains(i))
                attributs.+=(i)
            }
          
        }
        println(k._1+"    " +attributs.size)
        for(i<-attributs){
          var voisins=new MutableList[Int]()
          for(j<- 0 until nbrItem){
            var contentJ=tableau(j+1)(k._2);
            var elementsJ=contentJ.split(";");
            for(l<-elementsJ){
              if(i.equals(l)){
                voisins.+=(j);
              }
            }
          }
          for(j<-0 until voisins.size-1){
            for(l<-j+1 until voisins.size){
              if(!edge.contains((voisins(j),voisins(l)))){
                edge.+=(((voisins(j),voisins(l)),1))
              } else{
                edge.update((voisins(j),voisins(l)), (edge((voisins(j),voisins(l)))+1))
              }
            }
          }
        }
      }
    }
    
    var edges=new Array[Edge[Int]](edge.size);
    var cmpt=0;
    for(i<-edge){
      edges(cmpt)=Edge(i._1._1,i._1._2,i._2)
      cmpt+=1;
    }
    
    this.MakeJson(nodes,edges);
    return (nodes, edges)
  }
  
  def MakeJson(nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]]) {
    val file = new File("data.json")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write("{\n  \"graph\": [],\n  \"links\": [\n")
    
    for(i<-0 until edges.length){
      bw.write("	{\"source\": "+(edges(i).srcId)+", \"target\": "+(edges(i).dstId)+", \"weight\": \""+edges(i).attr+"\"}");
      if(i!=edges.size-1)
        bw.write(",\n")
    }
    bw.write("],\n  \"nodes\": [\n");
    
    for(i<-0 until nodes.length){
      //mettre circle ou square selon si label ou non **A FAIRE**
      bw.write("	{\"id\": \""+nodes(i)._2._1+"\", \"type\": "+"\"circle\""+", \"label\": \""+nodes(i)._2._2+"\"}");
      if(i!=nodes.size-1){
        bw.write(",\n")
      }
    }
    bw.write("],\n  \"directed\": false,\n  \"multigraph\": false\n}");
    bw.close();
  }
  
  // initialMsg is the user defined message that will be sent to the vertices prior to superstep 0
  var initialMsg = new MutableList[(String,Int)]
  initialMsg.+=(("",9999))

  // vprog is the user defined function for receiving messages
  def vprog(vertexId: VertexId, value: (String, String), message: (MutableList[(String,Int)])): (String, String) = {
    println("vprog")
    if (message == initialMsg) {
      return value
    } else {
      // construction des listes des labels et des poids associés
      var bufferLabels:MutableList[String]=new MutableList[String]();
      var bufferValue:MutableList[Int]=new MutableList[Int]();
      for (oneMessage:(String,Int)<-message){
        var buffer:String=oneMessage._1
        var elements=buffer.split(";");
        for(element:String<-elements){
          if(bufferLabels.contains(element) && !element.equals("")){
            bufferValue(bufferLabels.indexOf(element))+=oneMessage._2
          } else {
            bufferLabels.+=(element);
            bufferValue.+=(oneMessage._2)
          }
        }
      }
      
      // recherche du/des label(s) avec le point le plus important
      var messagesMax = new MutableList[(String)]
      var max = 0
      for (i<-0 until bufferLabels.size) {
        if (bufferValue(i) == max) {
          messagesMax.+=(bufferLabels(i))
        } else if (bufferValue(i) > max) {
          max = bufferValue(i)
          messagesMax.clear()
          messagesMax.+=(bufferLabels(i))
        }
      }
      
      // calcul de la nouvelle value
      if (messagesMax.length == 1) {
        return (value._1, messagesMax(0))
      } else {
        var labels = ""
        for (i<- 0 until messagesMax.length) {
          labels = labels + ";" + messagesMax(i)
        }
        return (value._1,labels)
      }
    }
  }
  
  // sendMsg is the user defined function to determine the messages
  // to send out for the next iteration and where to send it to
  def sendMsg(triplet: EdgeTriplet[(String, String), Int]): Iterator[(VertexId, MutableList[(String, Int)])] = {
    println("sendMsg")
    val sourceLabel = triplet.srcAttr._2
    val destinationLabel = triplet.dstAttr._2
    val destinationId = triplet.dstId
    val weight = triplet.attr
    
    if (destinationLabel != "") {
      return Iterator.empty
    } else {
      var messageToSend = new MutableList[(String,Int)]
      messageToSend.+=((sourceLabel,weight))
      return Iterator((destinationId, messageToSend))
    }
  }
  
  // mergeMsg is the user defined function to merge multiple messages arriving
  // at the same vertex at the start of a superstep before applying the vertex program vprog
  def mergeMsg(msg1: MutableList[(String,Int)], msg2: MutableList[(String,Int)]): MutableList[(String,Int)] = {
    println("mergeMsg")
    for (i<- 0 until msg2.length){
      msg1.+=(msg2(i))
    }
    return msg1
  }
  
  // lecture de data
  var lect:LectureData = new LectureData("data-train.tab");
  var data = lect.Parsing(150)
  val (nodes, edges) = this.BuildGraph(data, lect.correspondance)
  val nodess: RDD[(VertexId, (String, String))] = sc.parallelize(nodes)
  val relationships: RDD[Edge[Int]] = sc.parallelize(edges)
  val graph = Graph(nodess, relationships)
  println("Graph ok")
  //graph.vertices.collect.foreach(println)
  //graph.edges.collect.foreach(println)
  
  // application au graphe Int.MaxValue
  val newGraphe = graph.pregel(initialMsg, 
                            1000, 
                            EdgeDirection.Out)(
                            vprog,
                            sendMsg,
                            mergeMsg)
                            
  println("helloworld")
  newGraphe.vertices.collect.foreach(println)
  println("coucou")
  newGraphe.edges.collect.foreach(println)
  println("youpiyah")
                            
  def main(args:Array[String]) {
  }
}