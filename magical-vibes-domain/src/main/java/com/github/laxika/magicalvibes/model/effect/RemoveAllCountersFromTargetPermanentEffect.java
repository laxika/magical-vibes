package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Remove all counters from {target permanent | target creature | enchanted creature}", "target player loses all
 * [rad | poison] counters", "each opponent loses all counters", "remove all counters from up to one target permanent
 * or opponent".
 *
 * <p>One REMOVE_COUNTERS step: removes every counter of every concrete type from each permanent {@code subject} names,
 * and every counter of {@code playerCounterType} (every kind when {@code null}) from each player {@code players} names.
 * It records the total removed, the permanents and the players that lost counters (read by
 * {@code AmountThisWay(REMOVE_COUNTERS)}, "for each counter removed this way"), and also as the stack entry's event
 * value for a following {@code EventValue} effect. It does not stop counters being put on them again: that is
 * {@link CantGetCountersEffect}.</p>
 *
 * <p>{@link Subjects#target()} targets a permanent, or a permanent matching {@code targetFilter} ({@link
 * #fromTargetCreature()}: "target creature"). {@link Subjects#attached()} acts on the source Aura's host without
 * targeting it, so hexproof or protection does not stop it (Enchanted River's Grasp). A filter needs a target
 * subject. {@code subject} is {@code null} for a removal from players only ({@link #fromPlayers}); {@code players} is
 * {@code null} for a removal from permanents only.</p>
 */
public record RemoveAllCountersFromTargetPermanentEffect(PermanentSubject subject, PermanentPredicate targetFilter,
                                                         PlayerSubject players, PlayerCounterType playerCounterType)
        implements CardEffect {

    public RemoveAllCountersFromTargetPermanentEffect {
        if (subject == null && players == null) {
            subject = Subjects.target();
        }
        if (targetFilter != null && (subject == null || !subject.declaresTarget())) {
            throw new IllegalArgumentException("A target filter needs a target subject: " + subject);
        }
        if (playerCounterType != null && players == null) {
            throw new IllegalArgumentException("A player counter kind needs players");
        }
    }

    public RemoveAllCountersFromTargetPermanentEffect(PermanentSubject subject, PermanentPredicate targetFilter) {
        this(subject, targetFilter, null, null);
    }

    /** "Remove all counters from target permanent." */
    public RemoveAllCountersFromTargetPermanentEffect() {
        this(Subjects.target(), null);
    }

    /** "Remove all counters from {subject}", which it targets only if {@code subject} is a target. */
    public RemoveAllCountersFromTargetPermanentEffect(PermanentSubject subject) {
        this(subject, null);
    }

    /** "Remove all counters from target creature." */
    public static RemoveAllCountersFromTargetPermanentEffect fromTargetCreature() {
        return new RemoveAllCountersFromTargetPermanentEffect(Subjects.target(), new PermanentIsCreaturePredicate());
    }

    /** "[Players] lose all counters" ("target opponent loses all counters", "each opponent loses all counters"). */
    public static RemoveAllCountersFromTargetPermanentEffect fromPlayers(PlayerSubject players) {
        return new RemoveAllCountersFromTargetPermanentEffect(null, null, players, null);
    }

    /** "[Players] lose all [kind] counters" ("target player loses all rad counters"). */
    public static RemoveAllCountersFromTargetPermanentEffect fromPlayers(PlayerSubject players,
                                                                         PlayerCounterType kind) {
        return new RemoveAllCountersFromTargetPermanentEffect(null, null, players, kind);
    }

    /**
     * "Remove all counters from [permanents] or [players]", one target that may be either (Mutated Cultist's "up to
     * one target permanent or opponent", the target declared by the card's target filter).
     */
    public static RemoveAllCountersFromTargetPermanentEffect fromPermanentsAndPlayers(PermanentSubject permanents,
                                                                                      PlayerSubject players) {
        return new RemoveAllCountersFromTargetPermanentEffect(permanents, null, players, null);
    }

    @Override
    public TargetSpec targetSpec() {
        boolean permanentTarget = subject != null && subject.declaresTarget();
        boolean playerTarget = players != null && players.declaresTarget();
        if (permanentTarget && playerTarget) {
            return TargetSpec.benign(TargetPredicates.playerOrPermanent());
        }
        if (playerTarget) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (!permanentTarget) {
            return TargetSpec.NONE;
        }
        return TargetSpec.benign(targetFilter == null ? TargetPredicates.permanent()
                : TargetPredicates.permanents(targetFilter));
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
