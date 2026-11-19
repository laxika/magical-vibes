package com.github.laxika.magicalvibes.model.effect;

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
        PlayerSubject.OpponentsOf, PlayerSubject.ControllerOf, PlayerSubject.OwnerOf, PlayerSubject.TriggeringPlayer,
        PlayerSubject.ActivePlayer, PlayerSubject.DefendingPlayer, PlayerSubject.EnchantedPlayer,
        PlayerSubject.ChosenPlayer, PlayerSubject.ChosenPlayers, PlayerSubject.AcceptedPlayers, PlayerSubject.Except,
        PlayerSubject.AllOf {

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
}
