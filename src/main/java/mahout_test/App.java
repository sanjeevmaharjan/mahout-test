package mahout_test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.AbstractJDBCDataModel;
//import org.apache.mahout.cf.taste.impl.model.jdbc;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        FileDataModelExample();
        PostgresSQLJDBCDataModelExample();

    }

    private static void FileDataModelExample() throws Exception {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("intro.csv").toURI());
        DataModel model = new FileDataModel(file);

        recommend(model);
    }

    private static void PostgresSQLJDBCDataModelExample() throws Exception {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File propertiesFile = new File(classLoader.getResource("application.properties").toURI());
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            properties.load(fis);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName(properties.getProperty("datasource.server"));
        dataSource.setDatabaseName(properties.getProperty("datasource.database"));
        dataSource.setUser(properties.getProperty("datasource.user"));
        dataSource.setPassword(properties.getProperty("datasource.password"));
        dataSource.setPortNumber(Integer.parseInt(properties.getProperty("datasource.port")));

        AbstractJDBCDataModel dataModel = new PostgreSQLJDBCDataModel(dataSource, "prefs", "user_id", "cv_id", "views", null);

        recommend(dataModel);
    }

    private static void recommend(DataModel dataModel) throws Exception {
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);

        UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);

        Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(1, 2);

        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
    }
}
