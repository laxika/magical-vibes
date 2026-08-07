package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: exile the creature the source Equipment was attached to.
 *
 * <p>Placed in {@code EffectSlot.ON_DEATH} on an Equipment ("When this is put into a graveyard
 * from the battlefield, exile equipped creature" — Oathkeeper, Takeno's Daisho). The Equipment is
 * already gone from the battlefield when the trigger resolves, so its last-known attachment is
 * bound at collection time.
 *
 * @param equippedCreatureId the permanent ID of the equipped creature (null in the card definition)
 */
public record ExileEquippedCreatureEffect(UUID equippedCreatureId) implements CardEffect {

    public ExileEquippedCreatureEffect() {
        this(null);
    }
}
