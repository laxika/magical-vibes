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
        GameEventFact.DecisionRequested,
        GameEventFact.PrivateReveal, GameEventFact.MulliganResolved, GameEventFact.GameEnded {

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
    record DecisionRequested(
            UUID decisionId,
            UUID decidingPlayerId,
            DecisionKind decisionKind,
            DecisionDelivery delivery
    )
            implements GameEventFact {

        public DecisionRequested {
            Objects.requireNonNull(decisionId, "decisionId");
            Objects.requireNonNull(decidingPlayerId, "decidingPlayerId");
            Objects.requireNonNull(decisionKind, "decisionKind");
            Objects.requireNonNull(delivery, "delivery");
        }

        public DecisionRequested(UUID decisionId, UUID decidingPlayerId, DecisionKind decisionKind) {
            this(decisionId, decidingPlayerId, decisionKind, DecisionDelivery.OPENED);
        }

        @Override
        public GameEventKind kind() {
            return GameEventKind.DECISION_REQUESTED;
        }
    }

    /**
     * Existing public notification that one player's mulligan action resolved.
     */
    record MulliganResolved(UUID playerId, boolean kept, int mulliganCount)
            implements GameEventFact {

        public MulliganResolved {
            Objects.requireNonNull(playerId, "playerId");
            if (mulliganCount < 0) {
                throw new IllegalArgumentException("mulliganCount cannot be negative");
            }
        }

        @Override
        public GameEventKind kind() {
            return GameEventKind.MULLIGAN_RESOLVED;
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
     * Terminal runtime result. A win must name one winner; a draw or abandonment has none.
     */
    record GameEnded(GameResult result, UUID winnerPlayerId) implements GameEventFact {

        public GameEnded {
            Objects.requireNonNull(result, "result");
            if (result == GameResult.WIN && winnerPlayerId == null) {
                throw new IllegalArgumentException("A win must name the winning player");
            }
            if (result != GameResult.WIN && winnerPlayerId != null) {
                throw new IllegalArgumentException(result + " cannot name a winning player");
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

    enum DecisionDelivery {
        OPENED,
        REPLAY_REQUESTED
    }

    enum RevealZone {
        HAND,
        LIBRARY
    }

    enum GameResult {
        WIN,
        DRAW,
        /**
         * The runtime game was closed without producing a rules result, for example when every
         * player disconnected from a casual game or the human left a game against an AI.
         */
        ABANDONED
    }
}
