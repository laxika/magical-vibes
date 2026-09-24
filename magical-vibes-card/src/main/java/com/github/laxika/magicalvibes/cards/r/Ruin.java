package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Ruin — back half of Road // Ruin.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Ruin deals damage to target
 * creature equal to the number of lands you control.
 */
public class Ruin extends Card {

    public Ruin() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureEffect(
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
        addCastingOption(new FlashbackCast("{1}{R}{R}"));
    }
}
