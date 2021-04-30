import com.github.jsonldjava.shaded.com.google.common.collect.HashBasedTable;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.*;
import org.apache.jena.util.iterator.*;
import org.neo4j.driver.*;
import org.neo4j.driver.Driver;
import java.io.*;
import java.util.*;

public class infrencing {
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
       // HashMap<String, String> UrisToClassLabel = new HashMap<String, String>();
        List<List<String>> AllProps = new ArrayList<List<String>>();
        while (iterator.hasNext()) {
            OntClass ontClass = (OntClass) iterator.next();
            String uri = ontClass.getURI();
            String[] arrOfStr = uri.split("#", 2);
            System.out.println("\n\nClass is : " + arrOfStr[1]);
            classes.add(arrOfStr[1]);

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
//                System.out.println(prop.getLabel("en")+" "+UrisToClassLabel.get(domain)+" "+UrisToClassLabel.get(range));
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
        //hasmaps
        HashMap<String, Set<String>> stp = new HashMap<String, Set<String>>();
        HashMap<List<String>, Set<String>> pto = new HashMap<List<String>, Set<String>>();
        Iterator indi = model.listIndividuals();
        while (indi.hasNext()) {
            Individual indiv = (Individual) indi.next();
            System.out.println(indiv.getLocalName());
        }
        StmtIterator iterator1 = model.listStatements();
        int d = 0;
        HashMap<String,String> inverse=new HashMap<String, String>();
        ExtendedIterator<ObjectProperty> nodeIterator = model.listObjectProperties();
        while (nodeIterator.hasNext()) {
            ObjectProperty objectProperty = (ObjectProperty) nodeIterator.next();
            ExtendedIterator<? extends OntProperty> extendedIterator = objectProperty.listInverseOf();
            if (extendedIterator.hasNext()) {
                System.out.println("Object property is : " + objectProperty.getLocalName());
                System.out.println("Inverse Object property of: " + objectProperty.getInverseOf().getLocalName());
                inverse.put(objectProperty.getLocalName(),objectProperty.getInverseOf().getLocalName());
                inverse.put(objectProperty.getInverseOf().getLocalName(),objectProperty.getLocalName());
            }
        }
            while (iterator1.hasNext()) {
                System.out.println("*************************************");
                Statement statement = iterator1.nextStatement();
                Resource subject = statement.getSubject();
                Property predicate = statement.getPredicate();
                RDFNode object = statement.getObject();
                String sub, pred, obj;
                if (predicate.getLocalName().equals("label") || predicate.getLocalName().equals("rest") || predicate.getLocalName().equals("first") || predicate.getLocalName().equals("members")) {
                    continue;
                } else if (predicate.getLocalName().equals("ingredients") || predicate.getLocalName().equals("description") || predicate.getLocalName().equals("cusineName") || predicate.getLocalName().equals("startDate") || predicate.getLocalName().equals("endDate") || predicate.getLocalName().equals("cusineName") || predicate.getLocalName().equals("address") || predicate.getLocalName().equals("name")) {

                    System.out.println("Subject is: " + subject.getLocalName());
                    System.out.println("Predicate is: " + predicate.getLocalName());
                    System.out.println("Object is: " + object.toString());
                    sub = subject.getLocalName();
                    pred = predicate.getLocalName();
                    obj = object.toString();
//                if (!stp.containsKey(subject.getLocalName())) {
//                    stp.put(subject.getLocalName(), new ArrayList<String>());
//                }
//                stp.get(subject.getLocalName()).add(predicate.getLocalName());
//                List<String> list = new ArrayList<String>();
//                list.add(subject.getLocalName());
//                list.add(predicate.getLocalName());
//                if (!pto.containsKey(list)) {
//                    pto.put( list , new ArrayList<String>());
//                }
//                pto.get(list).add(object.toString());
                } else if (predicate.getLocalName().equals("price") || predicate.getLocalName().equals("city")) {
                    System.out.println("Subject is: " + subject.getLocalName());
                    System.out.println("Predicate is: " + predicate.getLocalName());
                    System.out.println("Object is: " + object.toString().replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#int", ""));
                    sub = subject.getLocalName();
                    pred = predicate.getLocalName();
                    obj = object.toString().replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#int", "");

//                    if (!stp.containsKey(subject.getLocalName())) {
//                        stp.put(subject.getLocalName(), new ArrayList<String>());
//                    }
//                    stp.get(subject.getLocalName()).add(predicate.getLocalName());
//                    List<String> list = new ArrayList<String>();
//                    list.add(subject.getLocalName());
//                    list.add(predicate.getLocalName());
//                    if (!pto.containsKey(list)) {
//                        pto.put(list, new ArrayList<String>());
//                    }
//                    pto.get(list).add(object.toString().replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#int", ""));
//                    //stp.get(predicate.getLocalName()).add(object.toString().replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#int", ""));
                } else {
                    System.out.println("Subject is: " + subject.getLocalName());
                    System.out.println("Predicate is: " + predicate.getLocalName());
                    String uri = object.toString();
                    String[] arrOfStr = uri.split("#", 2);
                    System.out.println("Object is: " + arrOfStr[1]);
                    //System.out.println("isko split krna pdhaega");
                    sub = subject.getLocalName();
                    pred = predicate.getLocalName();
                    obj = arrOfStr[1];
//                    if (!stp.containsKey(subject.getLocalName())) {
//                        stp.put(subject.getLocalName(), new ArrayList<String>());
//                    }
//                    stp.get(subject.getLocalName()).add(predicate.getLocalName());
//                    List<String> list = new ArrayList<String>();
//                    list.add(subject.getLocalName());
//                    list.add(predicate.getLocalName());
//                    if (!pto.containsKey(list)) {
//                        pto.put(list, new ArrayList<String>());
//                    }
//                    pto.get(list).add(arrOfStr[1]);
//                    // stp.get(predicate.getLocalName()).add(arrOfStr[1]);
//
//                }
                }
                if (!stp.containsKey(sub)) {
                        stp.put(sub,new HashSet<String>());
                    }
                    stp.get(sub).add(pred);
                    List<String> list = new ArrayList<String>();
                    list.add(sub);
                    list.add(pred);
                    if (!pto.containsKey(list)) {
                        pto.put(list, new HashSet<String>());
                    }
                    pto.get(list).add(obj);
                if(inverse.containsKey(pred))
                {
                    if (!stp.containsKey(obj)) {
                        stp.put(obj,new HashSet<String>());
                    }
                    stp.get(obj).add(inverse.get(pred));
                    List<String> list1 = new ArrayList<String>();
                    list1.add(obj);
                    list1.add(inverse.get(pred));
                    if (!pto.containsKey(list1)) {
                        pto.put(list1, new HashSet<String>());
                    }
                    pto.get(list1).add(sub);
                }
            }


            int a = 1;
            while (a == 1) {
                System.out.println("*************************************#####################*************************************");
                System.out.println("select subject from following list:");
                for (Map.Entry mapElement : stp.entrySet()) {
                    String key = (String) mapElement.getKey();
                    System.out.println(key);

                }
                Scanner scan = new Scanner(System.in);
                System.out.print("Enter your subject: ");
                String name_s = scan.nextLine();

                Set<String> list = new HashSet<String>();

                list = stp.get(name_s);
                System.out.println("select predicate from following list");
                System.out.println(list);
                System.out.print("Enter your predicate: ");
                String name_p = scan.nextLine();
                Set<String> list_o = new HashSet<String>();

                List<String> temp = new ArrayList<String>();
                temp.add(name_s);
                temp.add(name_p);
                list_o = pto.get(temp);

                System.out.println(list_o);

                System.out.println("FINAL RESULT\n\n");
                System.out.println("With subject '" + name_s + "' predicate '" + name_p + "' object is => " + list_o + "\n\n");
                System.out.println(name_s + " " + name_p + " " + list_o + "\n\n");

                System.out.println(" To Continue press 1 ");
                a = scan.nextInt();
            }

        }
    }
