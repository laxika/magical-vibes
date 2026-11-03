package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "288")
@CardRegistration(set = "ANB", collectorNumber = "39")
public class WaterkinShaman extends Card {

    public WaterkinShaman() {
        // Whenever a creature you control with flying enters (including this creature), this creature gets +1/+1 until end of turn.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasKeywordPredicate(Keyword.FLYING))),
                        ObjectRelation.YOU_CONTROL),
                new BoostSelfEffect(1, 1));
    }
}
