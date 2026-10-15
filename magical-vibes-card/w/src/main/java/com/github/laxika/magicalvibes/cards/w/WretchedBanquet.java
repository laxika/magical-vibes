package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLeastPowerAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Wretched Banquet — {B} Sorcery.
 * Destroy target creature if it has the least power or is tied for least power among creatures
 * on the battlefield.
 */
@CardRegistration(set = "CON", collectorNumber = "56")
public class WretchedBanquet extends Card {

    public WretchedBanquet() {
        // Any creature is a legal target; the least-power check is a resolution-time intervening-if,
        // so a target that lost the "least power" status by resolution simply does nothing.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentHasLeastPowerAmongAllCreaturesPredicate()),
                        new DestroyTargetPermanentEffect()));
    }
}
