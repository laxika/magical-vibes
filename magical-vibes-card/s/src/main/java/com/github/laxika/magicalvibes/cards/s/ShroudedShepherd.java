package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CleaveShadows;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "236")
public class ShroudedShepherd extends Card {

    public ShroudedShepherd() {
        setBackFaceCard(new CleaveShadows());
        addCastingOption(new AdventureCast("{1}{B}"));
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), BoostEffect.target(2, 2));
    }

    @Override
    public String getBackFaceClassName() {
        return "CleaveShadows";
    }
}
