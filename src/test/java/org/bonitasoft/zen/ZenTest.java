package org.bonitasoft.zen;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Zen.class })
public class ZenTest {

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
            count = checkIfTableExists();
            assertThat(count).isEqualTo(1L);
        } else {
            System.err.println("table TEST1 already exists, cleaning it up...");
            jdbcTemplate.execute("TRUNCATE TABLE test1");
        }
    }

    public void cleanupDBTable() {
        jdbcTemplate.execute("TRUNCATE TABLE test1");
    }

    private Long checkIfTableExists() {
        return jdbcTemplate.queryForObject("select COUNT(*) from INFORMATION_SCHEMA.TABLES t where LOWER (t.TABLE_NAME) ='test1'", Long.class);
    }

    @Test
    public void write_then_read() throws Exception {
        initDBTable();
        ExecutorService threadPoolExecutor = null;
        final int expected = 3000;
        try {
            threadPoolExecutor = new ThreadPoolExecutor(20, 20, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            for (int i = 0; i < expected; i++) {
                final Thread thread = new InsertRetrieveThread();
                threadPoolExecutor.submit(thread);
            }
        } finally {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdown();
                threadPoolExecutor.awaitTermination(10, TimeUnit.MINUTES);
            }
            System.err.println("Created and retrieved " + getCounter() + " elements");
            cleanupDBTable();
        }
    }

    class InsertRetrieveThread extends Thread {

        @Override
        public void run() {
            // Write + read xxx times in the same Thread:
            for (int i = 0; i < 10000; i++) {
                final int currentCounter = increaseCounter();
                final String newValue = "write_then_read_test_" + currentCounter;
                int nbRows = jdbcTemplate.update("INSERT INTO test1(text_value) VALUES(?)", newValue);
                assertThat(nbRows).isEqualTo(1);

                final Long value = jdbcTemplate.queryForObject("SELECT id FROM test1 WHERE text_value = ?", Long.class, newValue);
                assertThat(value).isEqualTo(Long.valueOf(currentCounter));

                // print every 1000 elements:
                if (currentCounter % 1000 == 0) {
                    System.out.println(Thread.currentThread().getName() + ". New record with ID " + value + " and text: " + newValue);
                }
            }
        }
    }

    synchronized int increaseCounter() {
        return ++cpt;
    }

    int getCounter() {
        return cpt;
    }

}
