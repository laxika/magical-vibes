package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "214")
@CardRegistration(set = "SLD", collectorNumber = "1525")
public class PriceOfGlory extends Card {

    public PriceOfGlory() {
        addTrigger(Triggers.playerTapsLandForMana(),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
    }
}
