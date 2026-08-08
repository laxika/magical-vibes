package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "BOK", collectorNumber = "19")
public class PatronOfTheKitsune extends Card {

    public PatronOfTheKitsune() {
        // Whenever a creature attacks, you may gain 1 life. Fires once per attacking creature,
        // regardless of who controls it or whom it attacks.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
