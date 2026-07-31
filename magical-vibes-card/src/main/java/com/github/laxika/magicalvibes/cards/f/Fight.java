package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Fight — back half of Prepare // Fight.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Target creature you control
 * fights target creature an opponent controls.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}. Stack entries keep the parent split card for exile disposition,
 * but {@code StackEntry.targetsForGroup} reads this half's target declarations when cast with
 * flashback so {@link FightTargetsEffect} can resolve both groups.
 */
public class Fight extends Card {

    public Fight() {
        // Target creature you control fights target creature an opponent controls.
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new FightTargetsEffect());
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{3}{G}"));
    }
}
