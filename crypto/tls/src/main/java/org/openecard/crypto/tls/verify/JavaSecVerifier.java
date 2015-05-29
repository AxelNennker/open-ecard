/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.crypto.tls.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.util.ArrayList;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.crypto.tls.CertificateVerificationException;
import org.openecard.crypto.tls.CertificateVerifier;


/**
 * Java Security based certificate verifier.
 * This implementation converts the BouncyCastle certificates to java.security certificates and uses the Java-bundled
 * PKIX mechanism to verify the certificate chain.
 *
 * @author Tobias Wich
 */
public class JavaSecVerifier implements CertificateVerifier {

    private final CertPathValidator certPathValidator;

    /**
     * Create a JavaSecVerifier and load the internal certificate path validator.
     *
     * @throws NoSuchAlgorithmException Thrown in case the validator could not be loaded due to a missing algorithm.
     */
    public JavaSecVerifier() throws NoSuchAlgorithmException {
	certPathValidator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
    }


    @Override
    public void isValid(Certificate chain, String hostname) throws CertificateVerificationException {
	try {
	    CertPath certPath = convertChain(chain);

	    // create the parameters for the validator
	    PKIXParameters params = new PKIXParameters(TrustStoreLoader.getTrustAnchors());
	    // disable CRL checking since we are not supplying any CRLs yet
	    params.setRevocationEnabled(false);

	    // validate - exception marks failure
	    certPathValidator.validate(certPath, params);
	} catch (CertPathValidatorException ex) {
	    throw new CertificateVerificationException(ex.getMessage());
	} catch (GeneralSecurityException ex) {
	    throw new CertificateVerificationException(ex.getMessage());
	} catch (IOException ex) {
	    throw new CertificateVerificationException("Error converting certificate chain to java.security format.");
	}
    }


    private CertPath convertChain(Certificate chain) throws CertificateException, IOException {
	final int numCerts = chain.getCertificateList().length;
	ArrayList<java.security.cert.Certificate> result = new ArrayList<>(numCerts);
	CertificateFactory cf = CertificateFactory.getInstance("X.509");

	for (org.openecard.bouncycastle.asn1.x509.Certificate next : chain.getCertificateList()) {
	    byte[] nextData = next.getEncoded();
	    ByteArrayInputStream nextDataStream = new ByteArrayInputStream(nextData);
	    java.security.cert.Certificate nextConverted = cf.generateCertificate(nextDataStream);
	    result.add(nextConverted);
	}

	return cf.generateCertPath(result);
    }

}
