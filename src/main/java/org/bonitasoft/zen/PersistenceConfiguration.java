package org.bonitasoft.zen;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Emmanuel Duchastenier
 */
@Configuration
public class PersistenceConfiguration {

    private static final String XA_BONITA_DATASOURCE = "java:jboss/datasources/bonitaDS";

    @Bean
    public DataSource dataSource() throws NamingException {
        return (DataSource) new InitialContext().lookup(XA_BONITA_DATASOURCE);
    }

}
