package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Injury — back half of Insult // Injury.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Injury deals 2 damage to target
 * creature and 2 damage to target player or planeswalker.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 *
 * <p>Uses {@link DealDamageToEachTargetEffect} (full amount to every chosen target) so resolution
 * reads the flat {@code targetIds} list — aftermath stack entries keep the parent split card,
 * whose empty {@code effectTargetIndexMap} would break per-group remapped {@code targetId}.
 */
public class Injury extends Card {

    public Injury() {
        // Cast-time filters: creature, then player or planeswalker.
        target(TargetFilters.creature());
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Second target must be a player or planeswalker"
        ));
        // 2 damage to each chosen target.
        addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new Fixed(2)));

        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{2}{R}"));
    }
}
