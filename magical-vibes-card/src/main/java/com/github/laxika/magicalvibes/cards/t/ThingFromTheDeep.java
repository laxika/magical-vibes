package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "POR", collectorNumber = "73")
public class ThingFromTheDeep extends Card {

    public ThingFromTheDeep() {
        // Whenever this creature attacks, sacrifice it unless you sacrifice an Island.
        addEffect(EffectSlot.ON_ATTACK, new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
