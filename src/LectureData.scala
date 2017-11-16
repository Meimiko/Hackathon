import scala.collection.mutable.HashMap
import scala.io.Source
import scala.util.control.Breaks
import scala.collection.mutable.MutableList


class LectureData(nom_fichier : String) {
  
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
  def RowSelection(rowList : Array[String]){
    for(row:String<-rowList){
      println(correspondance(row))
      var value:Int=correspondance(row);
      selectRow += (row->value);
      println(row);
    }
    
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
          simpleList1=simpleList1.take(j)
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
  
  
}