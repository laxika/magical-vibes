package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.UUID;

/** Turn-long Channel Harm prevention state for one player and a preselected creature target. */
public record ChannelHarmShield(UUID protectedPlayerId, Card sourceCard, UUID targetCreatureId) implements CardEffect {
}
