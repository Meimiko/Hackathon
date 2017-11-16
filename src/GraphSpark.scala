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
   
  }
  
  def MakeJson(nodes:Array[(VertexId, (String, String))],edges:Array[Edge[Int]]) {
    val file = new File("data.json")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write("{\n  \"graph\": [],\n  \"links\": [\n")
    
    for(i<-1 until edges.size){
      bw.write("	{\"source\": "+(edges(i).srcId-1)+", \"target\": "+(edges(i).dstId-1)+", \"weight\": \""+edges(i).attr+"\"}");
      if(i!=edges.size-1)
        bw.write(",\n")
    }
    bw.write("],\n  \"nodes\": [\n");
    
    for(i<-1 until nodes.size){
      //mettre circle ou square selon si label ou non **A FAIRE**
      bw.write("	{\"id\": \""+nodes(i)._1+"\", \"type\": "+"\"circle\""+", \"label\": \""+nodes(i)._2._2+"\"}");
      if(i!=nodes.size-1){
        bw.write(",\n")
      }
    }
    bw.write("],\n  \"directed\": false,\n  \"multigraph\": false\n}");
    bw.close();
  }
  
}