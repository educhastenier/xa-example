package org.bonitasoft.zen;

import static org.assertj.core.api.StrictAssertions.assertThat;

import javax.sql.DataSource;

import org.junit.Before;
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

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Before
    public void before() {

        new

        String object = jdbcTemplate.queryForObject("select @@version", String.class);
        assertThat(object).isNotNull().contains("Microsoft SQL Server");

        Long count = checkIfTableExists();
        if (count == 0) {
            jdbcTemplate.execute(
                    "create table test1(id numeric(19,0) identity (1,1) ,text_value nvarchar(50) )");
            count = checkIfTableExists();
        }

        assertThat(count).isEqualTo(1L);

    }

    private Long checkIfTableExists() {
        return jdbcTemplate.queryForObject("select COUNT(*) from INFORMATION_SCHEMA.TABLES t where LOWER (t.TABLE_NAME) ='test1'", Long.class);
    }

    @Test
    public void should_insert() {
        //when
        int nbRows = jdbcTemplate.update("insert into test1(text_value) values(?)", "text");

        //then
        assertThat(nbRows).isEqualTo(1);

    }

    @Test
    public void checkDriverVersion() throws Exception {
        //when
        String driverVersion = dataSource.getConnection().getMetaData().getDriverVersion();

        //then
        assertThat(driverVersion).isEqualTo("4.0.2206.100");

    }

}
