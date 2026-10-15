package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "AVR", collectorNumber = "126")
public class ArchwingDragon extends Card {

    public ArchwingDragon() {
        // Flying and haste are auto-loaded from Scryfall.
        // At the beginning of the end step, return this creature to its owner's hand.
        addEffect(EffectSlot.END_STEP_TRIGGERED, ReturnToHandEffect.self());
    }
}
