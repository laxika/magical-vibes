package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MaliciousInvader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureOrTransformSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "121")
@CardRegistration(set = "DBL", collectorNumber = "388")
public class InnocentTraveler extends Card {

    public InnocentTraveler() {
        setBackFaceCard(new MaliciousInvader());

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new AnyOpponentMaySacrificeCreatureOrTransformSourceEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "MaliciousInvader";
    }
}
