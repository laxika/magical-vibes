package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "At the beginning of each player's upkeep, that player may choose any number of tapped
 * [matching] creatures they control and pay {@code manaCost} for each creature chosen
 * this way. If the player does, untap those creatures." (Magnetic Mountain, Thelon's Curse.)
 *
 * <p>Placed in {@code EffectSlot.EACH_UPKEEP_TRIGGERED}; the acting player ("that player") is the
 * stack entry's target — the player whose upkeep it is, not the source's controller. On resolution
 * the acting player is offered a multi-permanent choice, capped by how many creatures they can
 * afford from their mana pool; the {@code manaCost} cost per chosen creature is paid and the
 * chosen creatures untap (see {@code MultiPermanentChoiceContext.PayManaPerCreatureUntap}).
 */
public record PayManaPerTappedCreatureToUntapEffect(String manaCost, PermanentPredicate creatureFilter)
        implements CardEffect {

    public PayManaPerTappedCreatureToUntapEffect(int manaPerCreature, PermanentPredicate creatureFilter) {
        this("{" + manaPerCreature + "}", creatureFilter);
    }
}
