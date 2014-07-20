/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.olingo.fit.proxy.v4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.v4.EdmEnabledODataClient;
import org.apache.olingo.commons.api.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.ext.proxy.Service;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.InMemoryEntities;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Address;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Employee;
import org.apache.olingo.fit.proxy.v4.staticservice.microsoft.test.odata.services.odatawcfservice.types.Person;
import org.junit.Test;

public class ContextTestITCase extends AbstractTestITCase {

  private void continueOnError(final Service<EdmEnabledODataClient> service, final InMemoryEntities container) {
    final Person person = service.newEntityInstance(Person.class);
    container.getPeople().add(person);

    final Employee employee = service.newEntityInstance(Employee.class);
    employee.setPersonID(199);
    employee.setFirstName("Fabio");
    employee.setLastName("Martelli");
    employee.setEmails(Collections.<String>singleton("fabio.martelli@tirasa.net"));
    final Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(2011, 3, 4, 9, 0, 0);
    employee.setDateHired(new Timestamp(date.getTimeInMillis()));
    final Address homeAddress = service.newComplex(Address.class);
    homeAddress.setCity("Pescara");
    homeAddress.setPostalCode("65100");
    homeAddress.setStreet("viale Gabriele D'Annunzio 256");
    employee.setHomeAddress(homeAddress);
    employee.setNumbers(Arrays.asList(new String[] {"3204725072", "08569930"}));

    container.getPeople().add(employee);

    final List<ODataRuntimeException> result = container.flush();
    assertEquals(2, result.size());
    assertTrue(result.get(0) instanceof ODataClientErrorException);
    assertNull(result.get(1));
  }

  @Test
  public void transactionalContinueOnError() {
    service.getClient().getConfiguration().setContinueOnError(true);
    continueOnError(service, container);
    service.getClient().getConfiguration().setContinueOnError(false);
  }

  @Test
  public void nonTransactionalContinueOnError() {
    final Service<EdmEnabledODataClient> _service = Service.getV4(testStaticServiceRootURL, false);
    _service.getClient().getConfiguration().setDefaultBatchAcceptFormat(ContentType.APPLICATION_OCTET_STREAM);
    _service.getClient().getConfiguration().setContinueOnError(true);

    final InMemoryEntities _container = _service.getEntityContainer(InMemoryEntities.class);

    continueOnError(_service, _container);
  }
}
