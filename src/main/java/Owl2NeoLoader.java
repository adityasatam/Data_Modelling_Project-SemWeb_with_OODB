import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.InputStream;
import java.util.*;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class Owl2NeoLoader {
    //    private static final String ONT_URL ="src/main/resources/Restaurant_Ontology.owl";
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
        model.read(in, null);

        ExtendedIterator<OntClass> iterator = model.listClasses();
        List<String> classes=new ArrayList<String>();
        HashMap<String,List<String>>ClassSuperclassUris=new HashMap<String,List<String>>();
        HashMap<String,String>UrisToClassLabel=new HashMap<String,String>();
        List<List<String>> AllProps=new ArrayList<List<String>>();
        HashMap<String,List<String>> Classproperties=new HashMap<String,List<String>>();
        while (iterator.hasNext()) {
            OntClass ontClass = (OntClass) iterator.next();
            String uri = ontClass.getURI();
            String[] arrOfStr = uri.split("#", 2);
            System.out.println("Class is : " + arrOfStr[1]);
            classes.add(arrOfStr[1]);


        }

//        //Creating Nodes...
//        session.run("CREATE (x:Restaurant {name:\"Pizza Hut\"}) ");
//        session.run("CREATE (x:Food {name:\"Veg Loaded\", ingredients:\"bread, cheese\", price:\"100\"}) ");
//        session.run("CREATE (x:Menu {StartDate:\"01-01-2021\", endDate:\"01-03-2021\"}) ");
//        session.run("CREATE (x:Cuisine {cuisineName:\"Italian\"}) ");
//        session.run("CREATE (x:Location {city:\"Delhi\", address:\"CP\"}) ");
//        session.run("CREATE (x:Restaurant {name:\"Domino's\"}) ");
//
//        //creating relationship
//        session.run("match(a:Restaurant), (b:Food) "+
//                "create (a)-[r:serves]->(b) return type(r)");
//
//        session.run("match(a:Food), (b:Restaurant) "+
//                "create (a)-[r:isServedIn]->(b) return type(r)");
//
//        session.run("match(a:Restaurant), (b:Location) "+
//                "create (a)-[r:isLocatedIn]->(b) return type(r)");
//
//        session.run("match(a:Restaurant), (b:Menu) "+
//                "create (a)-[r:hasMenu]->(b) return type(r)");
//
//        session.run("match(a:Restaurant), (b:Cuisine) "+
//                "create (a)-[r:isSpecializedIn]->(b) return type(r)");
//
//        session.close();
//        driver.close();
        }
    }
