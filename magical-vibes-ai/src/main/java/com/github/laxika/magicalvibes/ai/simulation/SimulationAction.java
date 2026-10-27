package com.github.laxika.magicalvibes.ai.simulation;

import java.util.List;
import java.util.UUID;

/**
 * Represents a discrete action the AI can take during MCTS simulation.
 * Each variant maps to a specific game operation (play card, declare attackers, etc.).
 */
public sealed interface SimulationAction {

    /**
     * Casts a spell. {@code handIndex} indexes the acting player's own hand, unless {@code commandCardId} names a
     * commander cast from the command zone or {@code handOwnerId} names the other player whose hand the card is
     * cast from (Sen Triplets).
     */
    record PlayCard(int handIndex, UUID targetId, int xValue, UUID commandCardId, UUID handOwnerId)
            implements SimulationAction {
        public PlayCard(int handIndex, UUID targetId, int xValue) { this(handIndex, targetId, xValue, null, null); }
        public PlayCard(int handIndex, UUID targetId, int xValue, UUID commandCardId) {
            this(handIndex, targetId, xValue, commandCardId, null);
        }
    }

    record PassPriority() implements SimulationAction {}

    record DeclareAttackers(List<Integer> attackerIndices) implements SimulationAction {}

    record DeclareBlockers(List<int[]> blockerAssignments) implements SimulationAction {}

    record ActivateAbility(UUID permanentId, int abilityIndex, UUID targetId) implements SimulationAction {}

    record ChooseCard(int cardIndex) implements SimulationAction {}

    record ChoosePermanent(UUID permanentId) implements SimulationAction {}

    record ChooseColor(String color) implements SimulationAction {}

    record MayAbilityChoice(boolean accept) implements SimulationAction {}
}
