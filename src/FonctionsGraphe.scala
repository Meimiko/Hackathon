import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import scala.collection.mutable.MutableList
import scala.Iterator
import main.GraphSpark


object App {
  // code d'exemple qui fonctionne
  /*val conf = new SparkConf().setAppName("test").setMaster("local")
  val sc = new SparkContext(conf)
  
  // create an RDD for the vertices
  val vertices: RDD[(VertexId, (String,String))] =
    sc.parallelize(Array((1L, ("nom1","label1")), (2L, ("nom2","label2")),
                         (3L, ("nom3","label3")), (4L, ("nom4","label4"))))
  
  // create an RDD for edges
  val relationships: RDD[Edge[Int]] =
    sc.parallelize(Array(Edge(1L, 2L, 1), Edge(1L, 4L, 4),
                         Edge(2L, 4L, 2), Edge(3L, 1L, 2), 
                         Edge(3L, 4L, 3)))
  
  // create the graph
  val graph = Graph(vertices, relationships)
  
  // check the graph
  graph.vertices.collect.foreach(println)*/
  
  // fin code d'exemple
  
  // code de Mael qu'on aimerait faire fonctionner dans cette classe
  val conf = new SparkConf().setAppName("SparkMe Application").setMaster("local")
  val sc = new SparkContext(conf)
  
  var lect:LectureData = new LectureData();
  var data = lect.Parsing("data-train.tab")
  
  var graphh:GraphSpark = new GraphSpark()
  var (nodes, edges) = graphh.BuildGraph(data)
  
  val vertices: RDD[(VertexId, (String, String))] = sc.parallelize(nodes)
  val relationships: RDD[Edge[Int]] = sc.parallelize(edges)
  
  var graph = Graph(vertices, relationships)
  // fin code Mael
  
  
  // traitement du graphe
  
  // initialMsg is the user defined message that will be sent to the vertices prior to superstep 0
  var initialMsg = new MutableList[(String,Int)]
  initialMsg.+=(("",9999))

  // vprog is the user defined function for receiving messages
  def vprog(vertexId: VertexId, value: (String, String), message: (MutableList[(String,Int)])): (String, String) = {
    if (message == initialMsg) {
      return value
    } else {
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
      if (messagesMax.length == 1) {
        return (value._1, messagesMax(0))
      } else {
        var labels = ""
        for (i<- 0 until messagesMax.length) {
          labels = labels + messagesMax(i)
        }
        return (value._1,labels)
      }
    }
  }
  
  // sendMsg is the user defined function to determine the messages
  // to send out for the next iteration and where to send it to
  def sendMsg(triplet: EdgeTriplet[(String, String), Int]): Iterator[(VertexId, MutableList[(String, Int)])] = {
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
    for (i<- 0 until msg2.length){
      msg1.+=(msg2(i))
    }
    return msg1
  }
  
  // application au graphe
  var newGraphe = graph.pregel(initialMsg, 
                            Int.MaxValue, 
                            EdgeDirection.Both)(
                            vprog,
                            sendMsg,
                            mergeMsg)
                            
                            
  def main(args:Array[String]) {
    // autre test code Mael
    /*var lect:LectureData = new LectureData();
    var data = lect.Parsing("data-train.tab")
    var graph:GraphSpark = new GraphSpark();
    graph.BuildGraph(data)*/
    // fin code Mael
  }
}