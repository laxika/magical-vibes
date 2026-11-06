package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: exile the creature the source Equipment was attached to.
 *
 * <p>Placed in "when this dies" on an Equipment ("When this is put into a graveyard
 * from the battlefield, exile equipped creature" — Oathkeeper, Takeno's Daisho). The Equipment is
 * already gone from the battlefield when the ability resolves, so the creature is the one the stack entry's
 * triggering permanent, the Equipment as it last existed on the battlefield (CR 603.10a), was attached to. An
 * Equipment that was attached to nothing still triggers and exiles nothing.
 */
public record ExileEquippedCreatureEffect() implements CardEffect {
}
