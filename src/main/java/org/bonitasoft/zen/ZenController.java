package org.bonitasoft.zen;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Emmanuel Duchastenier
 */
@RestController
@RequestMapping("api/")
public class ZenController {

    @RequestMapping(value = "run", method = RequestMethod.GET)
    public void runStress() throws Exception {
        write_then_read();
    }

    private static int cpt = 0;

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void initDBTable() {
        String object = jdbcTemplate.queryForObject("select @@version", String.class);
        assertThat(object).isNotNull().contains("Microsoft SQL Server");

        Long count = checkIfTableExists();
        if (count == 0) {
            System.err.println("table TEST1 does NOT already exist, creating it.");
            jdbcTemplate.execute(
                    "CREATE TABLE test1( id numeric(19,0) identity (1,1), text_value NVARCHAR(50) )");
            jdbcTemplate.execute(
                    "ALTER TABLE test1 ADD CONSTRAINT pk_test1 PRIMARY KEY (id)");
            count = checkIfTableExists();
            assertThat(count).isEqualTo(1L);
        } else {
            System.err.println("table TEST1 already exists, cleaning it up...");
            cleanupDBTable();
        }
    }

    public void cleanupDBTable() {
        jdbcTemplate.execute("TRUNCATE TABLE test1");
    }

    private Long checkIfTableExists() {
        return jdbcTemplate.queryForObject("select COUNT(*) from INFORMATION_SCHEMA.TABLES t where LOWER (t.TABLE_NAME) ='test1'", Long.class);
    }

    public void write_then_read() throws Exception {
        initDBTable();
        final int nbOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(nbOfThreads);
        for (int i = 0; i < nbOfThreads; i++) {
            executor.execute(new InsertAndRetrieveRecord(i));
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.err.println("Finished all threads");

        System.err.println("Created and retrieved " + getCounter() + " elements");
        //        cleanupDBTable();
    }

    class InsertAndRetrieveRecord extends Thread {

        InsertAndRetrieveRecord(int i) {
            setName("ManuWorkUnit" + i);
        }

        public void run() {
            // Write + read xxx times in the same Thread:
            for (int i = 0; i < 5000; i++) {
                final int currentCounter = increaseCounter();
                final String newValue = "write_then_read_test_" + currentCounter;
                int nbRows = jdbcTemplate.update("INSERT INTO test1(text_value) VALUES(?)", newValue);
                assertThat(nbRows).isEqualTo(1);

                final Long id = jdbcTemplate.queryForObject("SELECT id FROM test1 WHERE text_value = ?", Long.class, newValue);
                assertThat(id).isNotNull();
                final Long nbRecords = jdbcTemplate.queryForObject("SELECT count(id) FROM test1 WHERE id = ?", Long.class, id);
                assertThat(nbRecords).isEqualTo(1L);

                // print every xxx elements:
                if (currentCounter % 200 == 0) {
                    System.out.println(getName() + ". New record with ID " + id + " and text: " + newValue);
                }
            }
        }

    }

    private synchronized int increaseCounter() {
        return ++cpt;
    }

    private int getCounter() {
        return cpt;
    }

}
