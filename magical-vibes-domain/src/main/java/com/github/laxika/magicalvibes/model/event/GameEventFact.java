package com.github.laxika.magicalvibes.model.event;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable facts emitted by the game engine.
 *
 * <p>These records intentionally contain only identifiers, scalar values, and immutable
 * snapshots. Mutable domain objects such as {@code GameData}, {@code Card}, {@code Permanent},
 * and {@code StackEntry} must never be added here.
 */
public sealed interface GameEventFact permits GameEventFact.StateInvalidated,
        GameEventFact.DecisionRequested, GameEventFact.PrivateReveal, GameEventFact.GameEnded {

    GameEventKind kind();

    /**
     * A post-action observable-state invalidation. Multiple invalidations with the exact same
     * audience may be merged within one completed action.
     */
    record StateInvalidated(Set<StateSection> sections) implements GameEventFact {

        public StateInvalidated {
            sections = Set.copyOf(Objects.requireNonNull(sections, "sections"));
            if (sections.isEmpty()) {
                throw new IllegalArgumentException("At least one state section must be invalidated");
            }
        }

        public StateInvalidated(StateSection section) {
            this(Set.of(section));
        }

        @Override
        public GameEventKind kind() {
            return GameEventKind.STATE_INVALIDATED;
        }

        public StateInvalidated merge(StateInvalidated other) {
            EnumSet<StateSection> merged = EnumSet.copyOf(sections);
            merged.addAll(other.sections);
            return new StateInvalidated(merged);
        }
    }

    /**
     * A player decision that must be delivered independently of state refresh coalescing.
     * {@code decisionId} is the stable identity used for replay and answer correlation.
     */
    record DecisionRequested(UUID decisionId, UUID decidingPlayerId, DecisionKind decisionKind)
            implements GameEventFact {

        public DecisionRequested {
            Objects.requireNonNull(decisionId, "decisionId");
            Objects.requireNonNull(decidingPlayerId, "decidingPlayerId");
            Objects.requireNonNull(decisionKind, "decisionKind");
        }

        @Override
        public GameEventKind kind() {
            return GameEventKind.DECISION_REQUESTED;
        }
    }

    /**
     * An immutable snapshot of hidden cards revealed to a restricted audience.
     */
    record PrivateReveal(
            UUID revealId,
            UUID subjectPlayerId,
            RevealZone zone,
            List<CardSnapshot> cards
    ) implements GameEventFact {

        public PrivateReveal {
            Objects.requireNonNull(revealId, "revealId");
            Objects.requireNonNull(subjectPlayerId, "subjectPlayerId");
            Objects.requireNonNull(zone, "zone");
            cards = List.copyOf(Objects.requireNonNull(cards, "cards"));
        }

        @Override
        public GameEventKind kind() {
            return GameEventKind.PRIVATE_REVEAL;
        }
    }

    /**
     * Terminal game result. A draw has no winner; a win must name one.
     */
    record GameEnded(GameResult result, UUID winnerPlayerId) implements GameEventFact {

        public GameEnded {
            Objects.requireNonNull(result, "result");
            if (result == GameResult.WIN && winnerPlayerId == null) {
                throw new IllegalArgumentException("A win must name the winning player");
            }
            if (result == GameResult.DRAW && winnerPlayerId != null) {
                throw new IllegalArgumentException("A draw cannot name a winning player");
            }
        }

        @Override
        public GameEventKind kind() {
            return GameEventKind.GAME_ENDED;
        }
    }

    /**
     * Card identity captured at emission time. This is a value snapshot, not a domain Card.
     */
    record CardSnapshot(
            UUID cardId,
            String name,
            String setCode,
            String collectorNumber
    ) {

        public CardSnapshot {
            Objects.requireNonNull(cardId, "cardId");
            Objects.requireNonNull(name, "name");
        }
    }

    enum StateSection {
        GAME_STATUS,
        TURN_AND_PRIORITY,
        BATTLEFIELD,
        STACK,
        PLAYER_RESOURCES,
        PUBLIC_ZONES,
        PRIVATE_PLAYER_VIEW,
        PLAYABLE_ACTIONS,
        GAME_LOG
    }

    enum DecisionKind {
        INTERACTION,
        ATTACKER_DECLARATION,
        BLOCKER_DECLARATION,
        COMBAT_DAMAGE_ASSIGNMENT,
        MULLIGAN,
        CARDS_TO_BOTTOM
    }

    enum RevealZone {
        HAND,
        LIBRARY
    }

    enum GameResult {
        WIN,
        DRAW
    }
}
