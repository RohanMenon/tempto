/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.google.common.io.ByteSource;
import com.teradata.test.ProductTest;
import com.teradata.test.fulfillment.hive.tpch.TpchDataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.internal.fulfillment.hive.tpch.TpchEntityByteSource;
import io.airlift.tpch.TpchEntity;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.fulfillment.hive.tpch.TpchTable.NATION;
import static org.assertj.core.api.Assertions.assertThat;

public class TpchDataSourceTest
        extends ProductTest
{

    @Test(groups = "example_smoketest")
    public void testDataSource()
            throws IOException
    {
        TpchDataSource dataSource = new TpchDataSource(NATION, 1.0);

        String path =  "/product-test/" + dataSource.getPathSuffix();

        assertThat(path).isEqualTo("/product-test/tpch/sf-1_00/NATION");

        HdfsClient hdfsClient = testContext().getDependency(HdfsClient.class);
        String hdfsUsername = testContext().getDependency(String.class, "hdfs.username");

        Iterable<TpchEntity> generator = NATION.getTpchTableEntity().createGenerator(1.0, 1, 1);
        ByteSource byteSource = new TpchEntityByteSource<>(generator);
        String expectedData = IOUtils.toString(byteSource.openStream());
        hdfsClient.saveFile(path + "/data", hdfsUsername, byteSource.openStream());
        String storedData = hdfsClient.loadFile(path + "/data", hdfsUsername);

        assertThat(expectedData).isEqualTo(storedData);
    }
}
