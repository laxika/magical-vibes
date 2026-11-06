package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Triggered effect: put the creature card that died ("that card", the stack entry's triggering card) from its
 * owner's graveyard onto the battlefield under your control, tracked as a stolen creature so it stays under the new
 * controller's control. Does nothing if that card is no longer the graveyard object the event named (CR 400.7).
 * "You may return that card" is {@code new MayEffect(new ReturnDyingOpponentCreatureUnderYourControlEffect(), …)}
 * (Necroskitter, The Reaper King, No More); "you may pay {1}{B}. If you do, return that card" wraps it in a
 * {@link MayPayManaEffect} (Lim-Dûl the Necromancer).
 *
 * @param grantSubtype subtype granted to the returned permanent if it is a creature, or {@code null}
 */
public record ReturnDyingOpponentCreatureUnderYourControlEffect(CardSubtype grantSubtype) implements CardEffect {

    public ReturnDyingOpponentCreatureUnderYourControlEffect() {
        this(null);
    }
}
