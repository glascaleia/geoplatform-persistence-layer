/*
 *  geo-platform
 *  Rich webgis framework
 *  http://geo-platform.org
 * ====================================================================
 *
 * Copyright (C) 2008-2012 geoSDI Group (CNR IMAA - Potenza - ITALY).
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. This program is distributed in the 
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. You should have received a copy of the GNU General 
 * Public License along with this program. If not, see http://www.gnu.org/licenses/ 
 *
 * ====================================================================
 *
 * Linking this library statically or dynamically with other modules is 
 * making a combined work based on this library. Thus, the terms and 
 * conditions of the GNU General Public License cover the whole combination. 
 * 
 * As a special exception, the copyright holders of this library give you permission 
 * to link this library with independent modules to produce an executable, regardless 
 * of the license terms of these independent modules, and to copy and distribute 
 * the resulting executable under terms of your choice, provided that you also meet, 
 * for each linked independent module, the terms and conditions of the license of 
 * that module. An independent module is a module which is not derived from or 
 * based on this library. If you modify this library, you may extend this exception 
 * to your version of the library, but you are not obligated to do so. If you do not 
 * wish to do so, delete this exception statement from your version. 
 *
 */
package org.geosdi.geoplatform.persistence.configuration.jpa;

import java.util.Properties;
import javax.sql.DataSource;
import org.geosdi.geoplatform.persistence.configuration.properties.GPPersistenceConnector;
import org.geosdi.geoplatform.persistence.configuration.properties.GPPersistenceHibProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author Giuseppe La Scaleia - CNR IMAA geoSDI Group
 * @email giuseppe.lascaleia@geosdi.org
 */
@Configuration
@Profile(value = "jpa")
@EnableTransactionManagement
public class GPPersistenceJpaConfig {

    @Autowired
    private GPPersistenceConnector gpPersistenceConnector;
    //
    @Autowired
    private GPPersistenceHibProperties gpHibernateProperties;

    public LocalContainerEntityManagerFactoryBean gpEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean gpFactoryBean = new LocalContainerEntityManagerFactoryBean();
        gpFactoryBean.setDataSource(this.gpDataSource());
        gpFactoryBean.setPackagesToScan(
                this.gpPersistenceConnector.getPackagesToScan());

        final JpaVendorAdapter gpVendorAdapter = new HibernateJpaVendorAdapter() {
            {
                this.setDatabasePlatform(
                        gpHibernateProperties.getHibDatabasePlatform());
                this.setShowSql(gpHibernateProperties.isHibShowSql());
                this.setGenerateDdl(gpHibernateProperties.isHibGenerateDdl());
            }
        };

        gpFactoryBean.setJpaVendorAdapter(gpVendorAdapter);
        gpFactoryBean.setLoadTimeWeaver(this.gpLoadTimeWeaver());
        gpFactoryBean.setJpaProperties(gpJpaProperties());

        return gpFactoryBean;
    }

    /**
     * TODO : Change this implementation with {@link ComboPooledDataSource}
     */
    @Bean
    public DataSource gpDataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(
                this.gpPersistenceConnector.getDriverClassName());
        dataSource.setUrl(this.gpPersistenceConnector.getUrl());
        dataSource.setUsername(this.gpPersistenceConnector.getUsername());
        dataSource.setPassword(this.gpPersistenceConnector.getPassword());

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager gpTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                this.gpEntityManagerFactory().getObject());

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public LoadTimeWeaver gpLoadTimeWeaver() {
        return new InstrumentationLoadTimeWeaver();
    }

    final Properties gpJpaProperties() {
        return new Properties() {
            private static final long serialVersionUID = 3109256773218160485L;

            {
                this.put("persistence.dialect",
                         gpHibernateProperties.getHibDatabasePlatform());
                this.put("hibernate.hbm2ddl.auto",
                         gpHibernateProperties.getHibHbm2ddlAuto());
                this.put("hibernate.show_sql",
                         gpHibernateProperties.isHibShowSql());
                this.put("hibernate.cache.provider_class",
                         gpHibernateProperties.getHibCacheProviderClass());
                this.put("hibernate.cache.region.factory_class",
                         gpHibernateProperties.getHibCacheRegionFactoryClass());
                this.put("hibernate.cache.use_second_level_cache",
                         gpHibernateProperties.isHibUseSecondLevelCache());
                this.put("hibernate.cache.use_query_cache",
                         gpHibernateProperties.isHibUseQueryCache());
                this.put("hibernate.generate_statistics",
                         gpHibernateProperties.isHibGenerateStatistics());
                this.put("hibernate.default_schema",
                         gpHibernateProperties.getHibDefaultSchema());
                this.put("hibernate.default_schema",
                         gpHibernateProperties.getHibDefaultSchema());
                this.put("net.sf.ehcache.configurationResourceName",
                         gpHibernateProperties.getEhcacheConfResourceName());
            }
        };
    }
}
