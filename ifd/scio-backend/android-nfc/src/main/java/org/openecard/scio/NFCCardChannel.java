/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.scio;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.ifd.scio.SCIOCard;
import org.openecard.common.ifd.scio.SCIOChannel;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of smartcardio's cardChannel interface.
 *
 * @author Dirk Petrautzki
 */
public class NFCCardChannel implements SCIOChannel {

    private static final Logger logger = LoggerFactory.getLogger(NFCCardChannel.class);
    private final NFCCard card;
    private int lengthOfLastAPDU;

    public NFCCardChannel(NFCCard card) {
	this.card = card;
    }

    @Override
    public void close() throws SCIOException {
	logger.warn("close not supported");
	// we only have one channel and this will be open as long as we are connected to the tag
    }

    @Override
    public SCIOCard getCard() {
	return card;
    }

    @Override
    public int getChannelNumber() {
	return 0;
    }

    @Override
    public CardResponseAPDU transmit(CardCommandAPDU apdu) throws SCIOException {
	try {
	    lengthOfLastAPDU = apdu.toByteArray().length;
	    return new CardResponseAPDU(card.isodep.transceive(apdu.toByteArray()));
	} catch (IOException e) {
	    // TODO: check if the error code can be chosen more specifically
	    throw new SCIOException("Transmit failed", SCIOErrorCode.SCARD_F_UNKNOWN_ERROR, e);
	}
    }

    @Override
    public CardResponseAPDU transmit(byte[] apdu) throws SCIOException {
	return transmit(new CardCommandAPDU(apdu));
    }

    @Override
    public int transmit(ByteBuffer command, ByteBuffer response) throws SCIOException {
	CardCommandAPDU cca = new CardCommandAPDU(command.array());
	CardResponseAPDU cra = transmit(cca);
	byte[] data = cra.toByteArray();
	response.put(data);

	return data.length;
    }

    public int getLengthOfLastAPDU() {
	return lengthOfLastAPDU;
    }

    @Override
    public boolean isBasicChannel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isLogicalChannel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
