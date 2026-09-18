package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

@CardRegistration(set = "DMU", collectorNumber = "74")
public class VodaSeaScavenger extends Card {

    public VodaSeaScavenger() {
        // Domain — Look at the top X cards of your library, where X is the number of basic land
        // types among lands you control. You may put one of those cards on top of your library.
        // Put the rest on the bottom of your library in a random order.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LookAtTopCardsEffect(
                new BasicLandTypesAmongControlledLands(),
                new Fixed(1),
                null,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                false,
                LibrarySearchDestination.TOP_OF_LIBRARY,
                true));
    }
}
