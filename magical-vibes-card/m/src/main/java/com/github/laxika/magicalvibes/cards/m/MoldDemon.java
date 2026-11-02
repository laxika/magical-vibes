package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "112")
public class MoldDemon extends Card {

    public MoldDemon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
