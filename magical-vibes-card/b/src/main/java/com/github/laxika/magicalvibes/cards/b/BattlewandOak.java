package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "197")
public class BattlewandOak extends Card {

    public BattlewandOak() {
        // Whenever a Forest you control enters, this creature gets +2/+2 until end of turn.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.LAND,
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST))),
                        ObjectRelation.YOU_CONTROL),
                new BoostSelfEffect(2, 2));

        // Whenever you cast a Treefolk spell, this creature gets +2/+2 until end of turn.
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.TREEFOLK)),
                new BoostSelfEffect(2, 2));
    }
}
