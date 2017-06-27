package eu.europa.esig.dss.web.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.RemoteDocument;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.RestDocumentValidationService;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.validation.reports.dto.DataToValidateDTO;
import eu.europa.esig.dss.validation.reports.dto.ReportsDTO;
import eu.europa.esig.dss.web.config.CXFConfig;
import eu.europa.esig.jaxb.policy.ConstraintsParameters;

public class RestDocumentValidationIT extends AbstractIT {

	private RestDocumentValidationService validationService;

	@Before
	public void init() {
		validationService = JAXRSClientFactory.create(getBaseCxf() + CXFConfig.REST_VALIDATION, RestDocumentValidationService.class,
				Arrays.asList(new JacksonJsonProvider()));
	}

	@Test
	public void testWithNoPolicyAndNoOriginalFile() throws Exception {
		FileDocument fileDoc = new FileDocument(new File("src/test/resources/XAdESLTA.xml"));
		RemoteDocument signedFile = new RemoteDocument(Utils.toByteArray(fileDoc.openStream()), fileDoc.getMimeType(), fileDoc.getName(),
				fileDoc.getAbsolutePath());

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, null, null);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(2, result.getDiagnosticData().getSignatures().get(0).getTimestamps().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.TOTAL_PASSED);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithNoPolicyAndOriginalFile() throws Exception {

		FileDocument fileDoc = new FileDocument(new File("src/test/resources/xades-detached.xml"));
		RemoteDocument signedFile = new RemoteDocument(Utils.toByteArray(fileDoc.openStream()), fileDoc.getMimeType(), fileDoc.getName(),
				fileDoc.getAbsolutePath());

		FileDocument fileDoc2 = new FileDocument(new File("src/test/resources/sample.xml"));
		RemoteDocument originalFile = new RemoteDocument(Utils.toByteArray(fileDoc2.openStream()), fileDoc2.getMimeType(), fileDoc2.getName(),
				fileDoc2.getAbsolutePath());

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, null);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.TOTAL_FAILED);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithPolicyAndOriginalFile() throws Exception {

		FileDocument fileDoc = new FileDocument(new File("src/test/resources/xades-detached.xml"));
		RemoteDocument signedFile = new RemoteDocument(Utils.toByteArray(fileDoc.openStream()), fileDoc.getMimeType(), fileDoc.getName(),
				fileDoc.getAbsolutePath());

		FileDocument fileDoc2 = new FileDocument(new File("src/test/resources/sample.xml"));
		RemoteDocument originalFile = new RemoteDocument(Utils.toByteArray(fileDoc2.openStream()), fileDoc2.getMimeType(), fileDoc2.getName(),
				fileDoc2.getAbsolutePath());

		JAXBContext context = JAXBContext.newInstance(ConstraintsParameters.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		InputStream stream = new FileInputStream("src/test/resources/constraint.xml");
		ConstraintsParameters policy = (ConstraintsParameters) unmarshaller.unmarshal(stream);

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, originalFile, policy);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.TOTAL_FAILED);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

	@Test
	public void testWithPolicyAndNoOriginalFile() throws Exception {

		FileDocument fileDoc = new FileDocument(new File("src/test/resources/xades-detached.xml"));
		RemoteDocument signedFile = new RemoteDocument(Utils.toByteArray(fileDoc.openStream()), fileDoc.getMimeType(), fileDoc.getName(),
				fileDoc.getAbsolutePath());

		JAXBContext context = JAXBContext.newInstance(ConstraintsParameters.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		InputStream stream = new FileInputStream("src/test/resources/constraint.xml");
		ConstraintsParameters policy = (ConstraintsParameters) unmarshaller.unmarshal(stream);

		DataToValidateDTO toValidate = new DataToValidateDTO(signedFile, null, policy);

		ReportsDTO result = validationService.validateSignature(toValidate);

		assertNotNull(result.getDiagnosticData());
		assertNotNull(result.getDetailedReport());
		assertNotNull(result.getSimpleReport());

		assertEquals(1, result.getSimpleReport().getSignature().size());
		assertEquals(result.getSimpleReport().getSignature().get(0).getIndication(), Indication.INDETERMINATE);

		Reports reports = new Reports(result.getDiagnosticData(), result.getDetailedReport(), result.getSimpleReport());
		assertNotNull(reports);
	}

}
