package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

/**
 * Lanterns' Lift — back face of Lantern Bearer.
 * Enchant creature; enchanted creature gets +1/+1 and has flying; exile if it would go to the graveyard.
 */
public class LanternsLift extends Card {

    public LanternsLift() {
        // Enchant creature
        // Enchanted creature gets +1/+1 and has flying.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));

        // If Lanterns' Lift would be put into a graveyard from anywhere, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
