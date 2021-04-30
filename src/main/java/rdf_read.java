import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class rdf_read {
    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver(
                "bolt://localhost:7687", AuthTokens.basic("neo4j", Pass.pass));

        Session session = driver.session();

        // create an empty model
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        String ns = "http://www.w3.org/2002/07/owl#";
        // use the RDFDataMgr to find the input file
        InputStream in = RDFDataMgr.open("src/main/resources/Restaurant_Ontology.owl");
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: sample.rdf +  not found");
        }

// read the RDF/XML file
        model.read(in, null);

        ExtendedIterator<OntClass> iterator = model.listClasses();
        List<String> classes = new ArrayList<String>();
        HashMap<String, List<String>> ClassSuperclassUris = new HashMap<String, List<String>>();
        HashMap<String, String> UrisToClassLabel = new HashMap<String, String>();
        List<List<String>> AllProps = new ArrayList<List<String>>();
        while (iterator.hasNext()) {
            OntClass ontClass = (OntClass) iterator.next();
            String uri = ontClass.getURI();
            String[] arrOfStr = uri.split("#", 2);
            System.out.println("\n\nClass is : " + arrOfStr[1]);
            classes.add(arrOfStr[1]);
            UrisToClassLabel.put(ontClass.toString(), ontClass.getLabel("en"));
            if (ontClass.hasSuperClass()) {
//                System.out.println("SuperClassLabel is :"+ ontClass.getLabel("en"));
                System.out.println("SuperClass is : " + ontClass.getSuperClass());
                if (ClassSuperclassUris.containsKey(ontClass.toString())) {
                    List<String> temp = ClassSuperclassUris.get(ontClass.toString());
                    temp.add(ontClass.getSuperClass().toString());
                    ClassSuperclassUris.put(ontClass.getLabel("en"), temp);
                } else {
                    List<String> temp = new ArrayList<String>();
                    temp.add(ontClass.getSuperClass().toString());
                    System.out.println(ontClass.getLabel("en"));
                    ClassSuperclassUris.put(ontClass.getLabel("en"), temp);
                }

            }

            //Trying to get properties
            System.out.println("Properties->");
            ExtendedIterator<OntProperty> iterprop = ontClass.listDeclaredProperties();

            //ontClass.listAllOntProperties();

            while (iterprop.hasNext()) {
//                System.out.println(iterprop.next());
                OntProperty prop = (OntProperty) iterprop.next();
                String domain = "";
                domain = domain + prop.getDomain();
                String[] arrOfStr1 = domain.split("#", 2);
                domain = arrOfStr1[1];

                String range = "";
                range = range + prop.getRange();
                String[] arrOfStr2 = range.split("#", 2);
                range = arrOfStr2[1];

                List<String> temp = new ArrayList<String>();

                temp.add(domain);
                temp.add(prop.getLocalName());
                temp.add(range);
                AllProps.add(temp);
                System.out.println(domain + " " + prop.getLocalName() + " " + range);
            }
            //End of master while
        }

        System.out.println("\nClasses : ");
        for (int i = 0; i < classes.size(); i++) {
            System.out.println(classes.get(i));
        }
        System.out.println("\n");

        String match = "string";
        String match1 = "int";
        int i = 0, k = 0;
        String[] arr = new String[100], arr1 = new String[100], arrr = new String[100], arrr1 = new String[100];

//node attributes array arr
        for (List<String> innerList : AllProps) {
            if (innerList.contains(match) || innerList.contains(match1)) {
                arr[i++] = innerList.get(0);
                arr[i++] = innerList.get(1);
                arr[i++] = innerList.get(2);
            }
        }

        for (int j = 0; j < arr.length; j++) {
            if (arr[j] != null) {
                System.out.println("node attributes arr: " + arr[j]);
            }
        }

//relationships array arr1
        for (List<String> innerList1 : AllProps) {
            if (!innerList1.contains(match) && !innerList1.contains(match1)) {
                arr1[k++] = innerList1.get(0);
                arr1[k++] = innerList1.get(1);
                arr1[k++] = innerList1.get(2);
            }
        }

        System.out.println("\n");
        for (int l = 0; l < arr1.length; l++) {
            if (arr1[l] != null) {
                System.out.println("relationship arr1: " + arr1[l]);
            }
        }

        System.out.println("\n");
//======================================================================================================
//Individulas
        Iterator indi = model.listIndividuals();
        while (indi.hasNext()) {
            Individual indiv = (Individual) indi.next();

            System.out.println(indiv.getLocalName());
        }


        StmtIterator iterator1 = model.listStatements();
        int d = 0;
        while (iterator1.hasNext()) {
            System.out.println("*************************************");
            Statement statement = iterator1.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();
            System.out.println("Subject is: " + subject.getLocalName());
            System.out.println("Predicate is: " + predicate.getLocalName());
            System.out.println("Object is: " + object.toString());

//all nodes array arrr
            for (int z = 0; z < arr.length; z++) {
                if ((arr[z] != null) && (arr[z].equals(predicate.getLocalName()))) {
                    //System.out.println("matched string: "+ arr[--z] +" "+ arr[++z] +" "+object.toString());
                    arrr[d++] = arr[--z];
                    arrr[d++] = arr[++z];
                    arrr[d++] = object.toString();
                }
            }

            // System.out.println("Class is: " + statement.getClass().toString());
            if (object instanceof OntClass) {
                //  System.out.println("Object is: " + object.toString());
            }
        }
        ////////////////////////////////////
        System.out.println("\n");
//printing all nodes array arrr
        for (int j = 0; j < arrr.length; j++) {
            if (arrr[j] != null) {
                System.out.println("node arrr: " + arrr[j]);
            }
        }

        System.out.println("\n");

//storing class sizes in arr2
        int[] arr2 = new int[classes.size()];
        int count = 0;

        for (int t = 0; t < classes.size(); t++) {
            count = 0;
            for (int m = 0; m < arr.length; m++) {
                if ((arr[m] != null) && (arr[m].equals(classes.get(t)))) {
                    count++;
                }
                arr2[t] = count * 2 + 1;
            }
        }

//modifying all nodes array arrr1
        int[] arr3 = new int[100];
        int t = 0, j = 0;

        while ((t < arrr.length) && (arrr[t] != null) && (arrr[t + 3] != null)) {
            if (arrr[t].equals(arrr[t + 3])) {
                arr3[j++] = t + 3;
            }
            t = t + 3;
        }

        j = 0;
        while ((j < arr3.length) && (arr3[j] != 0)) {
            arrr[arr3[j++]] = null;
        }
        t = 0;
        for (j = 0; j < arrr.length; j++) {
            if (arrr[j] != null) {
                arrr1[t++] = arrr[j];
            }
        }

        for (j = 0; j < arrr1.length; j++) {
            if (arrr1[j] != null) {
                String s1 = arrr1[j];
                String s2 = s1.replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#int", "");
                ;
                arrr1[j] = s2;
            }
        }
        for (j = 0; j < arrr1.length; j++) {
            if (arrr1[j] != null) {
                System.out.println("modified node arrr1: " + arrr1[j]);
            }
        }
        System.out.println("\n");


        System.out.println("\n");
//Creating Nodes...
        for (t = 0; t < classes.size(); t++) {
            j=0;
            while((j < arrr1.length) && (arrr1[j]!=null)) {
                if(arrr1[j].equals(classes.get(t))){
                    switch((arr2[t]-1)/2){
                        case 0 :
                            break;
                        case 1 :
                            session.run("CREATE (x:" + arrr1[j]+ " {" + arrr1[j+1] + ":\"" + arrr1[j+2] + "\""+"}) ");
                            System.out.println("create node: "+" "+arrr1[j]+" "+arrr1[j+1]+" "+arrr1[j+2]);
                            j=j+arr2[0];
                            break;
                        case 2 :
                            session.run("CREATE (x:" + arrr1[j] + " {" + arrr1[j+1] + ":\"" + arrr1[j+2] + "\""+", " + arrr1[j+3] + ":\"" + arrr1[j+4] + "\""+"}) ");
                            System.out.println("create node: "+arrr1[j]+" "+arrr1[j+1]+" "+arrr1[j+2]+" "+arrr1[j+3]+" "+arrr1[j+4]);
                            j=j+arr2[2];
                            break;
                        case 3 :
                            session.run("CREATE (x:" + arrr1[j] + " {" + arrr1[j+1] + ":\"" + arrr1[j+2] + "\""+", " + arrr1[j+3] + ":\"" + arrr1[j+4] + "\""+", " + arrr1[j+5] + ":\"" + arrr1[j+6] + "\""+"}) ");
                            System.out.println("create node: "+arrr1[j]+" "+arrr1[j+1]+" "+arrr1[j+2]+" "+arrr1[j+3]+" "+arrr1[j+4]+" "+arrr1[j+5]+" "+arrr1[j+6]);
                            j=j+arr2[3];
                            break;
                        default :
                            System.out.println("add more switch cases");
                    }
                }
                j++;
            }
        }
        System.out.println("\n");
//Creating Relationship...
        int p = 0;
        while(p < arr1.length){
            if (arr1[p]!=null) {
                session.run("match(a:" + arr1[p] + "), (b:" + arr1[p+2] + ") " +
                        "create (a)-[r:" + arr1[p+1] + "]->(b) return type(r)");
                System.out.println("create relation: "+ arr1[p]+" "+arr1[p+2]+" "+arr1[p+1]);
                p=p+3;
            }
        }
    }

}