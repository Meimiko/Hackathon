import scala.collection.immutable.HashMap
import scala.io.Source
import scala.util.control.Breaks


class LectureData() {
  
  var correspondance:HashMap[String,Integer]=new HashMap[String,Integer]();
  correspondance+="Entry"->0;
  correspondance+="Entry name"->1;
  correspondance+="Gene ontology (GO)"->2;
  correspondance+="Taxonomic lineage (ALL)"->3;
  correspondance+="Cross-reference (Pfam)"->4;
  correspondance+="Cross-reference (InterPro)"->5;
  correspondance+="Cross-reference (CDD)"->6;
  correspondance+="Cross-reference (PIRSF)"->7;
  correspondance+="Cross-reference (PRINTS)"->8;
  correspondance+="Cross-reference (PROSITE)"->9;
  correspondance+="Cross-reference (ProDom)"->10;
  correspondance+="Cross-reference (SFLD)"->11;
  correspondance+="Cross-reference (SMART)"->12;
  correspondance+="Cross-reference (SUPFAM)"->13;
  correspondance+="Cross-reference (TIGRFAMs)"->14;
  correspondance+="Cross-reference (HAMAP)"->15;
  correspondance+="Cross-reference (Gene3D)"->16;
  correspondance+="Cross-reference (PANTHER)"->17;
  
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
  
  def Parsing(nom_fichier : String):Array[Array[String]]={
    var content=Source.fromFile(nom_fichier).getLines
    var data: Array[Array[String]]=new Array[Array[String]](1000)//Source.fromFile(nom_fichier).getLines.size);
    
    var cmpt=0;
    val loop = new Breaks;
    loop.breakable {
      for (line:String <- content) {
        var simpleList:Array[String]=new Array[String](correspondance.size);
        line.replace(",", ";");
        var line2:String=line.replace("	", "|");
        var elements=line2.split('|');
        while(elements.size<correspondance.size){
          elements:+="";
        }
        
        for(i<-0 until elements.size)
          simpleList(i)=elements(i)
        
        var simpleList1:Array[String]=simpleList;
        for((i,j)<-selectRow){
          simpleList1=simpleList1.take(j)
        }
        
        data(cmpt)=simpleList1;
        cmpt+=1;
        if(cmpt%100000==0){
          println(cmpt)
        } else if(cmpt>=1000){loop.break}
      }
    }
    return data;
  }
  
  
  
}