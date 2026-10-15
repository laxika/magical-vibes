package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "52")
public class Fettergeist extends Card {

    public Fettergeist() {
        // "At the beginning of your upkeep, sacrifice this creature unless you pay {1} for each
        // other creature you control." The base cost is {0} (always payable, so a lone Fettergeist
        // is never sacrificed); the generic increase counts the other creatures at resolution.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        PayManaCost.withGenericIncrease("{0}",
                                new PermanentCount(new PermanentIsCreaturePredicate(),
                                        CountScope.CONTROLLER, true)),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
