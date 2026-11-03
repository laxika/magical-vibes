package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentCountAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "84")
public class BogSerpent extends Card {

    public BogSerpent() {
        addStaticEffect(new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                "a Swamp"
        ));

        addTrigger(Triggers.sourceState(new PermanentControllerControlsPermanentCountAtMostPredicate(
                        0, new PermanentHasSubtypePredicate(CardSubtype.SWAMP))),
                new SacrificeSelfEffect());
    }
}
