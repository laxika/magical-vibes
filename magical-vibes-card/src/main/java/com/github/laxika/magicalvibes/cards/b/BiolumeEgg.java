package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.BiolumeSerpent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "INR", collectorNumber = "54")
public class BiolumeEgg extends Card {

    public BiolumeEgg() {
        setBackFaceCard(new BiolumeSerpent());

        // Defender is loaded from Scryfall.

        // When this creature enters, scry 2.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        // When you sacrifice this creature, return it to the battlefield transformed under its
        // owner's control at the beginning of the next end step.
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedReturnSourceTransformedEffect(true, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "BiolumeSerpent";
    }
}
