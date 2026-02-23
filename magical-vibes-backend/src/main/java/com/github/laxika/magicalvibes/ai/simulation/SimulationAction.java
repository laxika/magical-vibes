package com.github.laxika.magicalvibes.ai.simulation;

import java.util.List;
import java.util.UUID;

/**
 * Represents a discrete action the AI can take during MCTS simulation.
 * Each variant maps to a specific game operation (play card, declare attackers, etc.).
 */
public sealed interface SimulationAction {

    record PlayCard(int handIndex, UUID targetPermanentId) implements SimulationAction {}

    record PassPriority() implements SimulationAction {}

    record DeclareAttackers(List<Integer> attackerIndices) implements SimulationAction {}

    record DeclareBlockers(List<int[]> blockerAssignments) implements SimulationAction {}

    record ActivateAbility(UUID permanentId, int abilityIndex, UUID targetPermanentId) implements SimulationAction {}

    record ChooseCard(int cardIndex) implements SimulationAction {}

    record ChoosePermanent(UUID permanentId) implements SimulationAction {}

    record ChooseColor(String color) implements SimulationAction {}

    record MayAbilityChoice(boolean accept) implements SimulationAction {}
}
