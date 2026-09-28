package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles the top card of the controller's library, then offers two choices: get two energy
 * counters, or decline and gain permission to play that exiled card until end of turn.
 *
 * @param exiledCardId the card exiled by the resolved effect, or {@code null} on the card definition
 */
public record ExileTopCardMayGetEnergyOrPlayThisTurnEffect(UUID exiledCardId) implements CardEffect {

    public ExileTopCardMayGetEnergyOrPlayThisTurnEffect() {
        this(null);
    }
}
