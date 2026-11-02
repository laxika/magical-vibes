package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "POR", collectorNumber = "73")
@CardRegistration(set = "AA4", collectorNumber = "9")
@CardRegistration(set = "ME4", collectorNumber = "68")
public class ThingFromTheDeep extends Card {

    public ThingFromTheDeep() {
        // Whenever this creature attacks, sacrifice it unless you sacrifice an Island.
        addTrigger(Triggers.attacks(), new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
