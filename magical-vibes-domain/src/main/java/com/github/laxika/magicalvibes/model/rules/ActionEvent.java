package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VotingResult;

import java.util.List;
import java.util.UUID;

/**
 * A player takes a keyword or game action, or one is taken with an object: the events "whenever you roll one or more
 * dice", "whenever an opponent commits a crime", "whenever a creature you control explores", "when this creature
 * exploits a creature" abilities trigger on (CR 603.2). Each is an occurrence of its own, emitted as the action is
 * taken; the abilities that trigger record what it carries.
 */
public sealed interface ActionEvent extends RulesEvent {

    /** {@code playerId} set {@code scheme} in motion (CR 701.32a: Archenemy). */
    record SchemeSetInMotion(StackEntry scheme, UUID playerId) implements ActionEvent {
        @Override
        public SchemeSetInMotion deepCopy() {
            return new SchemeSetInMotion(new StackEntry(scheme), playerId);
        }
    }

    /** {@code playerId} gave a gift. */
    record GiftGiven(UUID playerId) implements ActionEvent {
        @Override
        public GiftGiven deepCopy() {
            return this;
        }
    }

    /** {@code playerId} solved {@code solvedCase}. */
    record CaseSolved(Permanent solvedCase, UUID playerId) implements ActionEvent {
        @Override
        public CaseSolved deepCopy() {
            return new CaseSolved(new Permanent(solvedCase), playerId);
        }
    }

    /** {@code playerId} foretold {@code card}. */
    record Foretold(UUID playerId, Card card) implements ActionEvent {
        @Override
        public Foretold deepCopy() {
            return this;
        }
    }

    /** {@code playerId} foraged. */
    record Foraged(UUID playerId) implements ActionEvent {
        @Override
        public Foraged deepCopy() {
            return this;
        }
    }

    /** The players finished a vote with {@code result}. */
    record VotingFinished(VotingResult result) implements ActionEvent {
        @Override
        public VotingFinished deepCopy() {
            return this;
        }
    }

    /** The Ring tempted {@code playerId}, who chose {@code ringBearerId} as their Ring-bearer. */
    record RingBearerChosen(UUID playerId, UUID ringBearerId) implements ActionEvent {
        @Override
        public RingBearerChosen deepCopy() {
            return this;
        }
    }

    /** The Ring tempted {@code playerId}. */
    record TemptedByRing(UUID playerId) implements ActionEvent {
        @Override
        public TemptedByRing deepCopy() {
            return this;
        }
    }

    /** {@code playerId} investigated, for the first time this turn when {@code firstThisTurn}. */
    record Investigated(UUID playerId, boolean firstThisTurn) implements ActionEvent {
        @Override
        public Investigated deepCopy() {
            return this;
        }
    }

    /**
     * {@code playerId} rolled {@code diceCount} dice at once, {@code result} the result where one die was rolled; the
     * planar die when {@code planar}.
     */
    record DiceRolled(UUID playerId, int diceCount, int result, boolean planar) implements ActionEvent {
        @Override
        public DiceRolled deepCopy() {
            return this;
        }
    }

    /** One die with {@code sides} faces that {@code playerId} rolled came up {@code result}, its natural result. */
    record DieRolled(UUID playerId, int sides, int result) implements ActionEvent {
        @Override
        public DieRolled deepCopy() {
            return this;
        }
    }

    /** {@code playerId} rolled a natural 20. */
    record Natural20Rolled(UUID playerId) implements ActionEvent {
        @Override
        public Natural20Rolled deepCopy() {
            return this;
        }
    }

    /** {@code playerId} manifested dread and put {@code cardPutIntoGraveyard} (or none) into their graveyard. */
    record ManifestedDread(UUID playerId, Card cardPutIntoGraveyard) implements ActionEvent {
        @Override
        public ManifestedDread deepCopy() {
            return this;
        }
    }

    /** {@code playerId} completed a dungeon (CR 309.7). */
    record DungeonCompleted(UUID playerId) implements ActionEvent {
        @Override
        public DungeonCompleted deepCopy() {
            return this;
        }
    }

    /** {@code playerId} waterbent, earthbent, firebent or airbent. */
    record Bent(UUID playerId, BendingType type) implements ActionEvent {
        @Override
        public Bent deepCopy() {
            return this;
        }
    }

    /** {@code playerId} exerted the creature {@code creatureId} (CR 701.43). */
    record Exerted(UUID playerId, UUID creatureId) implements ActionEvent {
        @Override
        public Exerted deepCopy() {
            return this;
        }
    }

    /** {@code playerId} exerted {@code creature} as it attacked (CR 701.43d). */
    record ExertedAsItAttacks(UUID playerId, Permanent creature) implements ActionEvent {
        @Override
        public ExertedAsItAttacks deepCopy() {
            return new ExertedAsItAttacks(playerId, new Permanent(creature));
        }
    }

    /** {@code playerId} collected evidence. */
    record EvidenceCollected(UUID playerId) implements ActionEvent {
        @Override
        public EvidenceCollected deepCopy() {
            return this;
        }
    }

    /** {@code playerId} discovered {@code value} (CR 701.57a). */
    record Discovered(UUID playerId, int value) implements ActionEvent {
        @Override
        public Discovered deepCopy() {
            return this;
        }
    }

    /** {@code playerId} sought {@code cards}. */
    record Sought(UUID playerId, List<Card> cards) implements ActionEvent {
        public Sought {
            cards = List.copyOf(cards);
        }

        @Override
        public Sought deepCopy() {
            return this;
        }
    }

    /** {@code playerId} conjured {@code count} cards. */
    record Conjured(UUID playerId, int count) implements ActionEvent {
        @Override
        public Conjured deepCopy() {
            return this;
        }
    }

    /** {@code playerId} became the monarch. */
    record BecameMonarch(UUID playerId) implements ActionEvent {
        @Override
        public BecameMonarch deepCopy() {
            return this;
        }
    }

    /** {@code playerId} won a coin flip. */
    record CoinFlipWon(UUID playerId) implements ActionEvent {
        @Override
        public CoinFlipWon deepCopy() {
            return this;
        }
    }

    /** {@code playerId} lost a coin flip. */
    record CoinFlipLost(UUID playerId) implements ActionEvent {
        @Override
        public CoinFlipLost deepCopy() {
            return this;
        }
    }

    /** {@code payingPlayerId} paid {@code manaPaid} mana of a tax {@code taxingPlayerId}'s object imposes. */
    record TaxPaid(UUID payingPlayerId, UUID taxingPlayerId, int manaPaid) implements ActionEvent {
        @Override
        public TaxPaid deepCopy() {
            return this;
        }
    }

    /** {@code playerId} clashed and won it when {@code won} (CR 701.30). */
    record Clashed(UUID playerId, boolean won) implements ActionEvent {
        @Override
        public Clashed deepCopy() {
            return this;
        }
    }

    /** {@code playerId} lost the game. */
    record LostGame(UUID playerId) implements ActionEvent {
        @Override
        public LostGame deepCopy() {
            return this;
        }
    }

    /**
     * {@code playerId} sacrificed one or more permanents (CR 701.21a): one event for the permanents a player sacrificed
     * at once.
     */
    record Sacrificed(UUID playerId) implements ActionEvent {
        @Override
        public Sacrificed deepCopy() {
            return this;
        }
    }

    /** {@code playerId} committed a crime (CR 700.13). */
    record CrimeCommitted(UUID playerId) implements ActionEvent {
        @Override
        public CrimeCommitted deepCopy() {
            return this;
        }
    }

    /** {@code creature}, controlled by {@code controllerId}, fought. */
    record Fought(Permanent creature, UUID controllerId) implements ActionEvent {
        @Override
        public Fought deepCopy() {
            return new Fought(new Permanent(creature), controllerId);
        }
    }

    /** A creature {@code controllerId} controls explored, revealing {@code exploredCard} ({@code null} for none). */
    record Explored(UUID controllerId, Card exploredCard) implements ActionEvent {
        @Override
        public Explored deepCopy() {
            return this;
        }
    }

    /**
     * {@code creature} connived, controlled by {@code controllerId}: its last-known controller (CR 701.50b), or
     * {@code null} for its controller now.
     */
    record Connived(Permanent creature, UUID controllerId) implements ActionEvent {
        @Override
        public Connived deepCopy() {
            return new Connived(new Permanent(creature), controllerId);
        }
    }

    /**
     * A creature exploited a creature (CR 702.110a): {@code sourceCard}, the permanent {@code sourcePermanentId}
     * controlled by {@code sourceControllerId} as it exploited, sacrificed {@code exploitedCard}, whose power was
     * {@code exploitedPower}, its sacrifice chosen by {@code exploitingPlayerId}; {@code exploiter} is the exploiting
     * permanent as the sacrifice was chosen. {@code exploitedCard} is {@code null} for an event that only "when this
     * exploits a creature" watches.
     */
    record Exploited(Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId, UUID exploitingPlayerId,
                     Permanent exploiter, Card exploitedCard, int exploitedPower) implements ActionEvent {
        @Override
        public Exploited deepCopy() {
            return new Exploited(sourceCard, sourceControllerId, sourcePermanentId, exploitingPlayerId,
                    exploiter == null ? null : new Permanent(exploiter), exploitedCard, exploitedPower);
        }
    }

    /** A creature was championed with {@code source}, controlled by {@code controllerId}. */
    record Championed(Permanent source, UUID controllerId) implements ActionEvent {
        @Override
        public Championed deepCopy() {
            return new Championed(new Permanent(source), controllerId);
        }
    }

    /** {@code playerId} plotted {@code card} (CR 702.170a). */
    record Plotted(UUID playerId, Card card) implements ActionEvent {
        @Override
        public Plotted deepCopy() {
            return this;
        }
    }

    /**
     * {@code permanent}, controlled by {@code controllerId}, mutated (CR 702.140): {@code mutatedCards} are the cards
     * whose "whenever this creature mutates" abilities trigger.
     */
    record Mutated(Permanent permanent, List<Card> mutatedCards, UUID controllerId) implements ActionEvent {
        public Mutated {
            mutatedCards = List.copyOf(mutatedCards);
        }

        @Override
        public Mutated deepCopy() {
            return new Mutated(new Permanent(permanent), mutatedCards, controllerId);
        }
    }
}
