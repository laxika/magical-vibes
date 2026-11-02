package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MoonrageBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "7")
@CardRegistration(set = "DBL", collectorNumber = "7")
public class BrutalCathar extends Card {

    public BrutalCathar() {
        setBackFaceCard(new MoonrageBrute());

        target(TargetFilters.creatureAnOpponentControls());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTargetPermanentUntilSourceLeavesEffect());
        addTrigger(Triggers.transformsToFrontFace(ObjectRelation.THIS),
                new ExileTargetPermanentUntilSourceLeavesEffect());

    }

    @Override
    public String getBackFaceClassName() {
        return "MoonrageBrute";
    }
}
