package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "74")
public class IcyPrison extends Card {

    public IcyPrison() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // "When this enchantment enters, exile target creature."
                // "When this enchantment leaves the battlefield, return the exiled card..."
                // — LTB return is implicit via O-ring linkage.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentUntilSourceLeavesEffect())
                // "At the beginning of your upkeep, sacrifice this enchantment unless any player pays {3}."
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new ForcedCostOrElseEffect(
                                new PayManaCost("{3}"),
                                List.of(new SacrificeSelfEffect()),
                                true,
                                true));
    }
}
