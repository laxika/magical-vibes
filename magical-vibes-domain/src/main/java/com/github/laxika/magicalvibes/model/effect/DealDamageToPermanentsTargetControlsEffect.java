package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals a fixed amount of damage to each permanent {@code subject} names: by default each creature controlled by the
 * targeted player, or by the controller of the targeted planeswalker ("each creature that player or that planeswalker's
 * controller controls", Chandra's Fury); with a wider filter, every creature and planeswalker (Chandra, Bold
 * Pyromancer's −7).
 * Non-targeting — piggybacks on a companion targeting effect (e.g. a target-player DealDamageToPlayersEffect)
 * that provides the target on the same stack entry, or on an explicit {@code target(...)} declaration on the card.
 *
 * @param damage amount dealt to each permanent the subject names
 * @param subject which permanents are damaged: a {@link Subjects#controlledBy} set; the effect does nothing when its
 *        players name nobody
 * @param damagedCreaturesMustAttackThisTurn when true, each creature actually dealt damage this way
 *        must attack this turn if able (transient flag, cleared at end of turn). Creatures whose
 *        damage was fully prevented or redirected are untouched, which is what "each creature dealt
 *        damage this way" requires (Aggravate)
 */
public record DealDamageToPermanentsTargetControlsEffect(int damage,
                                                         PermanentSubject subject,
                                                         boolean damagedCreaturesMustAttackThisTurn)
        implements CardEffect {

    /** "That player or that planeswalker's controller", read from the target another effect or the card declares. */
    private static final PlayerSubject TARGETED_PLAYER = Subjects.allOf(Subjects.targetPlayerDeclaredElsewhere(),
            Subjects.controllerOf(Subjects.target()));

    public DealDamageToPermanentsTargetControlsEffect {
        if (!(subject instanceof PermanentSubject.ControlledBy)) {
            throw new IllegalArgumentException("Damage to the permanents a player controls needs a controlledBy "
                    + "subject: " + subject);
        }
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage) {
        this(damage, false);
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage, boolean damagedCreaturesMustAttackThisTurn) {
        this(damage, Subjects.controlledBy(TARGETED_PLAYER, new PermanentIsCreaturePredicate()),
                damagedCreaturesMustAttackThisTurn);
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage, PermanentSubject subject) {
        this(damage, subject, false);
    }

    /** {@code damage} to each permanent matching {@code filter} that the targeted player controls. */
    public static DealDamageToPermanentsTargetControlsEffect toTargetedPlayers(
            int damage, PermanentPredicate filter) {
        return new DealDamageToPermanentsTargetControlsEffect(damage, Subjects.controlledBy(TARGETED_PLAYER, filter));
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, true, null, false, 1);
    }
}
