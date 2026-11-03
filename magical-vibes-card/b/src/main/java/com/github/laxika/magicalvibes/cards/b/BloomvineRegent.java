package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ClaimTerritory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "136")
public class BloomvineRegent extends Card {

    public BloomvineRegent() {
        setBackFaceCard(new ClaimTerritory());
        addCastingOption(new OmenCast());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new GainLifeEffect(3));
    }

    @Override
    public String getBackFaceClassName() {
        return "ClaimTerritory";
    }
}
