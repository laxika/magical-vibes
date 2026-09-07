package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MaliciousInvader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureOrTransformSourceEffect;

@CardRegistration(set = "VOW", collectorNumber = "121")
public class InnocentTraveler extends Card {

    public InnocentTraveler() {
        setBackFaceCard(new MaliciousInvader());

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new AnyOpponentMaySacrificeCreatureOrTransformSourceEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "MaliciousInvader";
    }
}
