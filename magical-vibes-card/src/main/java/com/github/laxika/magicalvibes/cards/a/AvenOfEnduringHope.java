package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "HOU", collectorNumber = "5")
public class AvenOfEnduringHope extends Card {

    public AvenOfEnduringHope() {
        // Flying is auto-loaded from Scryfall as a keyword.
        // When this creature enters, you gain 3 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }
}
