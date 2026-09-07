package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DauntlessAvenger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "MID", collectorNumber = "4")
public class BereavedSurvivor extends Card {

    public BereavedSurvivor() {
        setBackFaceCard(new DauntlessAvenger());

        // When another creature you control dies, transform this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DauntlessAvenger";
    }
}
