package main


import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import scala.collection.mutable.MutableList
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import scala.collection.mutable.HashMap
import main._

class GraphSpark {
  
  /*def BuildGraph(tableau : Array[Array[String]]): (Array[(VertexId, (String, String))],Array[Edge[Int]]) ={
    var nbrItem=tableau.length-1
    var nbrCol=tableau(0).length
    
    var nodes=new Array[(VertexId, (String, String))](nbrItem);
    
    //Creation de tout les noeuds
    var cmptId:VertexId=0;
    for(i<- 0 until nbrItem){
      nodes(i)=(cmptId, (tableau(i+1)(0), tableau(i+1)(2)));
      cmptId+=1;
    }
    
    var edges=new Array[Edge[Int]](0);
    
    for(i<- 0 until nbrItem-1){
      var links=new MutableList[Edge[Int]]();
      for(j<- i+1 until nbrItem){
        var value=0;
        for(k<-4 until nbrCol){
          var contentI=tableau(i+1)(k);
          var contentJ=tableau(j+1)(k);
          if(!contentI.equals("") | !contentJ.equals((""))){
            var elementsI=contentI.split(";");
            var elementsJ=contentJ.split(";");
            for(l<-elementsI){
              for(m<-elementsJ){
                if(l.equals(m))
                  value+=1;
              }
            }
          }
        }
        if(value>0)
            links.+=(Edge(i,j,value))
      }
      if(links.size>0){
        var edgeBuff=new Array[Edge[Int]](links.size)
        for(o<-0 until links.size)
          edgeBuff(o)=links(o);
        edges=edges++edgeBuff
      }
      if((i-1)%1000==0)
        println(i)
    }
    
    
    /*val conf = new SparkConf().setAppName("SparkMe Application").setMaster("local")
    val sc = new SparkContext(conf)
    
    val nodess: RDD[(VertexId, (String, String))] =sc.parallelize(nodes)
    val relationships: RDD[Edge[Int]] =sc.parallelize(edges)*/
    //val graph = Graph(nodess, relationships)
    
    //graph.vertices.collect.foreach(println)
    this.MakeJson(nodes,edges);
    return (nodes,edges)
   
  }*/
  
  
  
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
      if(k._2>2){
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
    return (nodes,edges)
  }
  
  def MakeJson(nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]])={
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
    println("Json construit")
  }
  
  def searchProtein(protein:String,nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]])={
    val conf = new SparkConf().setAppName("SparkMe Application").setMaster("local")
    val sc = new SparkContext(conf)
  
    val vertices: RDD[(VertexId, (String, String))] = sc.parallelize(nodes)
    val relationships: RDD[Edge[Int]] = sc.parallelize(edges)
    
    var graphes=Graph(vertices,relationships);
    var proteine:VertexId=0;
    for(i<-nodes){
      if(i._2._1.equals(protein))
        proteine=i._1;
    }
    this.MakeJson(nodes, edges)
    graphes.collectNeighbors(EdgeDirection.Either).lookup(proteine)(0).foreach(println)
    graphes.collectEdges(EdgeDirection.Either).lookup(proteine)(0).foreach(println)
    graphes.vertices.collect()
    graphes.edges.collect()
  }
  
  def propagationLabel(nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]])={
    
    
  }
  

}