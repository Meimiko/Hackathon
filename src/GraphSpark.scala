package main


import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import scala.collection.mutable.MutableList
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

 class GraphSpark {
  
  def BuildGraph(tableau : Array[Array[String]]): (Array[(VertexId, (String, String))],Array[Edge[Int]]) ={
    var nbrItem=tableau.length
    var nbrCol=tableau(0).length
    
    var nodes=new Array[(VertexId, (String, String))](nbrItem);
    
    //Creation de tout les noeuds
    var cmptId:VertexId=0;
    for(i<- 1 until nbrItem){
      nodes(i)=(cmptId, (tableau(i)(0), tableau(i)(2)));
      cmptId+=1;
    }
    
    var edges=new Array[Edge[Int]](0);
    
    for(i<- 1 until nbrItem-1){
      var links=new MutableList[Edge[Int]]();
      for(j<- i+1 until nbrItem){
        var value=0;
        for(k<-4 until nbrCol){
          var contentI=tableau(i)(k);
          var contentJ=tableau(j)(k);
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
      if(i%1000==0)
        println(i)
    }
    
    
    /*val conf = new SparkConf().setAppName("SparkMe Application").setMaster("local")
    val sc = new SparkContext(conf)
    
    val nodess: RDD[(VertexId, (String, String))] =sc.parallelize(nodes)
    val relationships: RDD[Edge[Int]] =sc.parallelize(edges)*/
    //val graph = Graph(nodess, relationships)
    
    //graph.vertices.collect.foreach(println)
    return (nodes,edges)
   //this.MakeJson(nodes,edges);
  }
  
  def MakeJson(nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]]) {
    val file = new File("test.txt")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write("{\n	\"graph\": [],\n	\"links\": [\n")
    
    for(i<-0 until edges.size){
      bw.write("{\"source\": "+edges(i).srcId+", \"target\": "+edges(i).dstId+", \"weight\": \""+edges(i).attr+"\"},");
      if(i!=edges.size-1)
        bw.write("\n")
    }
    bw.write("]\n		\"nodes\": [\n");
    for(i<-0 until edges.size){
      bw.write("{\"source\": "+edges(i).srcId+", \"target\": "+edges(i).dstId+", \"weight\": \""+edges(i).attr+"\"},");
    }
    
    

    bw.close()
  }
  
}