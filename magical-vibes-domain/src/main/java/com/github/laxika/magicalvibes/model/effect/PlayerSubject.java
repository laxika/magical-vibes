package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Which players an effect acts on: the effect's verb names the action, this value names who it happens to. Built
 * through {@link Subjects}; resolved only by the engine's {@code SubjectResolutionService}. Several players are always
 * returned in APNAP order (CR 101.4).
 *
 * <p>Only a declared {@link TargetPlayer} declares a target ({@link #declaresTarget()}); a target nested in
 * {@link ControllerOf} or {@link OwnerOf} reads the stack entry's target without declaring one. A subject that reads a
 * stack entry ({@link #needsStackEntry()}) cannot be used by a static ability.
 *
 * <p>The variants and the wording each stands for are listed in {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public sealed interface PlayerSubject permits PlayerSubject.You, PlayerSubject.TargetPlayer, PlayerSubject.Each,
        PlayerSubject.OpponentsOf, PlayerSubject.ControllerOf, PlayerSubject.OwnerOf, PlayerSubject.OwnersOfCards, PlayerSubject.TriggeringPlayer,
        PlayerSubject.ActivePlayer, PlayerSubject.DefendingPlayer, PlayerSubject.EnchantedPlayer,
        PlayerSubject.ChosenPlayer, PlayerSubject.ChosenPlayers, PlayerSubject.AcceptedPlayers, PlayerSubject.ModeChooser,
        PlayerSubject.PlayersDealtDamage, PlayerSubject.SubgameWinners, PlayerSubject.Except, PlayerSubject.AllOf, PlayerSubject.Matching,
        PlayerSubject.PlayersThisWay, PlayerSubject.Voted {

    /** Whether this subject is the effect's own player target, chosen as the spell or ability is put on the stack. */
    default boolean declaresTarget() {
        return false;
    }

    /** Whether resolving this subject reads a stack entry, so a static ability can't use it. */
    default boolean needsStackEntry() {
        return false;
    }

    /** Which players {@link Each} names. */
    enum Relation {
        /** Every player, the controller included. */
        ALL,
        /** Every opponent of the controller. */
        OPPONENTS
    }

    /** "You": the controller of the spell or ability (of the source permanent, for a static ability). */
    record You() implements PlayerSubject {
    }

    /**
     * "Target player": the effect's player target. {@code group} is the target group the effect reads, or
     * {@code null} for the targets the stack entry assigns to the effect. {@code declared} is whether this effect
     * declares the target; when it doesn't, another effect of the same spell or ability, or the card's target filter,
     * declares it and this one only reads it ("target player draws two cards and loses 2 life").
     */
    record TargetPlayer(Integer group, boolean declared) implements PlayerSubject {

        public TargetPlayer {
            if (group != null && group < 0) {
                throw new IllegalArgumentException("A target group is not negative: " + group);
            }
        }

        @Override
        public boolean declaresTarget() {
            return declared;
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /** "Each player", "each opponent", in APNAP order. */
    record Each(Relation relation) implements PlayerSubject {

        public Each {
            Objects.requireNonNull(relation, "relation");
        }
    }

    /**
     * "Each of that player's opponents": every player other than those {@code player} names, in APNAP order; nobody
     * when {@code player} names nobody.
     */
    record OpponentsOf(PlayerSubject player) implements PlayerSubject {

        public OpponentsOf {
            Objects.requireNonNull(player, "player");
        }

        @Override
        public boolean needsStackEntry() {
            return player.needsStackEntry();
        }
    }

    /**
     * "Its controller": the controller of each permanent {@code permanent} names, as it is now, or as it last existed
     * on the battlefield once it has left (CR 608.2h).
     */
    record ControllerOf(PermanentSubject permanent) implements PlayerSubject {

        public ControllerOf {
            Objects.requireNonNull(permanent, "permanent");
        }

        @Override
        public boolean needsStackEntry() {
            return permanent.needsStackEntry();
        }
    }

    /** "Its owner": the owner of each permanent {@code permanent} names, read from its card as it is or last was. */
    record OwnerOf(PermanentSubject permanent) implements PlayerSubject {

        public OwnerOf {
            Objects.requireNonNull(permanent, "permanent");
        }

        @Override
        public boolean needsStackEntry() {
            return permanent.needsStackEntry();
        }
    }

    /**
     * "The exiled card's owner": the owner of each card {@code cards} names as the effect resolves, each once, in
     * APNAP order; nobody when it names no card.
     */
    record OwnersOfCards(CardSubject cards) implements PlayerSubject {

        public OwnersOfCards {
            Objects.requireNonNull(cards, "cards");
            if (cards.declaresTarget()) {
                throw new IllegalArgumentException("The owner of a target card is that card's scope: " + cards);
            }
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /** "That player": the player the trigger event was about, recorded on the stack entry; not a target. */
    record TriggeringPlayer() implements PlayerSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /** "The active player": the player whose turn it is as the effect is applied. */
    record ActivePlayer() implements PlayerSubject {
    }

    /** "Defending player": the player the source's attack named on the stack entry was against. */
    record DefendingPlayer() implements PlayerSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /** "Enchanted player": the player the source Aura (a Curse) is attached to. */
    record EnchantedPlayer() implements PlayerSubject {
    }

    /**
     * "The chosen player": the player the source permanent chose as it entered; while the enters ability that chooses
     * it ({@link RememberTargetPlayerEffect}) is still on the stack, that ability's target.
     */
    record ChosenPlayer() implements PlayerSubject {
    }

    /** "The chosen players": every player the source permanent chose as it entered, in APNAP order. */
    record ChosenPlayers() implements PlayerSubject {
    }

    /**
     * The players who said yes to the preceding "each player may" choice of the same resolution, as the stack entry
     * recorded them ("each opponent who discarded their hand this way"); a step result.
     */
    record AcceptedPlayers() implements PlayerSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That player" of "an opponent chooses one —": the opponent who chose the spell's mode as it was cast
     * ({@link ChooseOneEffect.ModeChooser#AN_OPPONENT}, CR 700.2e), as the stack entry recorded them.
     */
    record ModeChooser() implements PlayerSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * The players an earlier step of the same resolution dealt damage to, as the stack entry recorded them ("each
     * player dealt damage this way", Chandra, Roaring Flame); a player whose damage was all prevented is not one. A
     * step result.
     */
    record PlayersDealtDamage() implements PlayerSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * "That player", "each player who [drew | discarded | milled | lost life] this way", "its controller" of what a step
     * removed: the players the most recent {@code kind} step of the same resolution recorded on the stack entry's
     * resolution scratchpad (the players it happened to, or the controllers, owners for cards, of what it acted on as
     * it acted, CR 608.2h; see {@link StepKind}). Not for {@link StepKind#DEAL_DAMAGE} ({@link PlayersDealtDamage}),
     * {@link StepKind#COUNTER_SPELL} ({@code controllerOf(targetSpell())}) or {@link StepKind#COPY_SPELL}. A step
     * result.
     *
     * <p>With a {@code cardFilter}, only the players of the recorded cards that match it, each card judged where it is
     * now (wherever the step or a replacement put it) and naming the player whose zone holds it: its owner, whose hand,
     * library, graveyard or exile it can only be in (CR 400.3), or its controller where a replacement put it onto the
     * battlefield. "Each opponent who didn't discard a nonland card this way" is
     * {@code except(eachOpponent(), playersThisWay(DISCARD, nonland))} (Kroxa, Titan of Death's Hunger). With a
     * {@code permanentFilter}, only the controllers of the recorded permanents that match it, each as it is on the
     * battlefield or as it last existed there (CR 608.2h): "each player who sacrificed a Plains this way" (Desolation).
     *
     * @param kind            the step kind whose most recent step is read
     * @param cardFilter      which recorded cards name their owner; {@code null} for no card filter
     * @param permanentFilter which recorded permanents name their controller; {@code null} for no permanent filter
     */
    record PlayersThisWay(StepKind kind, CardPredicate cardFilter, PermanentPredicate permanentFilter)
            implements PlayerSubject {

        public PlayersThisWay(StepKind kind) {
            this(kind, null, null);
        }

        public PlayersThisWay {
            Objects.requireNonNull(kind, "kind");
            switch (kind) {
                case DEAL_DAMAGE -> throw new IllegalArgumentException(
                        "Players dealt damage this way are Subjects.playersDealtDamageThisResolution()");
                case COUNTER_SPELL -> throw new IllegalArgumentException(
                        "The countered spell's controller is Subjects.controllerOf(Subjects.targetSpell())");
                case COPY_SPELL -> throw new IllegalArgumentException("A copy step records no player");
                default -> {
                }
            }
            if (cardFilter != null && permanentFilter != null) {
                throw new IllegalArgumentException("A step's players are filtered by their cards or by their "
                        + "permanents, not both");
            }
            if (cardFilter != null && !EnumSet.of(StepKind.DISCARD, StepKind.MILL, StepKind.DRAW, StepKind.REVEAL)
                    .contains(kind)) {
                throw new IllegalArgumentException("A " + kind + " step's players are not the owners of cards");
            }
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * The players who won the subgame an earlier step of the same resolution played, as the stack entry recorded its
     * result (Shahrazad: "each player who doesn't win that game" is {@code except(eachPlayer(), subgameWinners())}); a
     * step result. Resolving it without a recorded result is an engine error.
     */
    record SubgameWinners() implements PlayerSubject {

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }

    /**
     * The players {@code players} names but those {@code excluded} names: "the other chosen player" is
     * {@code except(chosenPlayers(), triggeringPlayer())}.
     */
    record Except(PlayerSubject players, PlayerSubject excluded) implements PlayerSubject {

        public Except {
            Objects.requireNonNull(players, "players");
            Objects.requireNonNull(excluded, "excluded");
            if (players.declaresTarget() || excluded.declaresTarget()) {
                throw new IllegalArgumentException("An exception can't declare a target: " + players + ", "
                        + excluded);
            }
        }

        @Override
        public boolean needsStackEntry() {
            return players.needsStackEntry() || excluded.needsStackEntry();
        }
    }

    /**
     * Every player any of {@code subjects} names, each once: "that player or that planeswalker's controller" is
     * {@code allOf(targetPlayerDeclaredElsewhere(), controllerOf(target()))} over a "target player or planeswalker".
     */
    record AllOf(List<PlayerSubject> subjects) implements PlayerSubject {

        public AllOf {
            subjects = List.copyOf(subjects);
            if (subjects.size() < 2) {
                throw new IllegalArgumentException("AllOf joins at least two subjects: " + subjects);
            }
            if (subjects.stream().anyMatch(PlayerSubject::declaresTarget)) {
                throw new IllegalArgumentException("A union of players can't declare a target: " + subjects);
            }
        }

        @Override
        public boolean needsStackEntry() {
            return subjects.stream().anyMatch(PlayerSubject::needsStackEntry);
        }
    }

    /**
     * The players {@code players} names who match {@code filter} as the effect is applied (CR 608.2h): "each player who
     * controls the most lands" is {@code matching(eachPlayer(), new PlayerControlsMostLandsPredicate())}. The filter is
     * evaluated with the effect's controller as the comparing player.
     */
    record Matching(PlayerSubject players, PlayerPredicate filter) implements PlayerSubject {

        public Matching {
            Objects.requireNonNull(players, "players");
            Objects.requireNonNull(filter, "filter");
            if (players.declaresTarget()) {
                throw new IllegalArgumentException("A filtered set of players can't declare a target: " + players);
            }
        }

        @Override
        public boolean needsStackEntry() {
            return players.needsStackEntry();
        }
    }

    /**
     * "Each player who received no votes" ({@link VoteEffect.Tally#NONE}), "each player with the most votes"
     * ({@link VoteEffect.Tally#MOST}), "each player who received a vote" ({@link VoteEffect.Tally#ANY}): the players in
     * the game, by the votes each received in the most recent vote of the same resolution ({@code VoteEffect}), in APNAP
     * order. A step result.
     *
     * @param tally which players are named
     */
    record Voted(VoteEffect.Tally tally) implements PlayerSubject {

        public Voted {
            Objects.requireNonNull(tally, "tally");
        }

        @Override
        public boolean needsStackEntry() {
            return true;
        }
    }
}
