package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "191")
@CardRegistration(set = "VOW", collectorNumber = "196")
@CardRegistration(set = "DBL", collectorNumber = "463")
public class DawnhartDisciple extends Card {

    public DawnhartDisciple() {
        // Whenever another Human you control enters, this creature gets +1/+1 until end of turn.
        // "Another" is implicit — the entering creature never triggers its own ally-enter slot.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.HUMAN))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new BoostSelfEffect(1, 1));
    }
}
