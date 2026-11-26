package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * Deals damage to each permanent {@code subject} names: by default each creature controlled by the targeted player, or
 * by the controller of the targeted planeswalker ("each creature that player or that planeswalker's controller
 * controls", Chandra's Fury); with a wider filter, every creature and planeswalker (Chandra, Bold Pyromancer's −7);
 * with {@code controlledBy(eachOpponent(), ...)} each creature and planeswalker the opponents control (Goblin
 * Chainwhirler); with {@link Subjects#otherCreaturesSharing} each other creature sharing a color, a name or a creature
 * type with the target (Brightflame, Homing Lightning, Killer).
 * Non-targeting — piggybacks on a companion targeting effect (e.g. a target-player DealDamageEffect.to(targetPlayer(), n))
 * that provides the target on the same stack entry, or on an explicit {@code target(...)} declaration on the card.
 * "X and Y" in one sentence is one damage event: add it beside the companion with {@code Card.addSimultaneousEffects}
 * or {@link SequenceEffect#simultaneous}. It begins a {@link com.github.laxika.magicalvibes.model.StepKind#DEAL_DAMAGE}
 * step ("you gain life equal to the damage dealt this way").
 *
 * @param damage amount dealt to each permanent the subject names, evaluated once as the effect resolves
 * @param subject which permanents are damaged: a {@link Subjects#controlledBy} set (the effect does nothing when its
 *        players name nobody) or {@link Subjects#otherCreaturesSharing}
 * @param damagedCreaturesMustAttackThisTurn when true, each creature actually dealt damage this way
 *        must attack this turn if able (transient flag, cleared at end of turn). Creatures whose
 *        damage was fully prevented or redirected are untouched, which is what "each creature dealt
 *        damage this way" requires (Aggravate)
 */
public record DealDamageToPermanentsTargetControlsEffect(DynamicAmount damage,
                                                         PermanentSubject subject,
                                                         boolean damagedCreaturesMustAttackThisTurn)
        implements CardEffect {

    /** "That player or that planeswalker's controller", read from the target another effect or the card declares. */
    private static final PlayerSubject TARGETED_PLAYER = Subjects.allOf(Subjects.targetPlayerDeclaredElsewhere(),
            Subjects.controllerOf(Subjects.target()));

    public DealDamageToPermanentsTargetControlsEffect {
        Objects.requireNonNull(damage, "damage");
        if (!(subject instanceof PermanentSubject.ControlledBy
                || subject instanceof PermanentSubject.OtherPermanentsSharing)) {
            throw new IllegalArgumentException("Damage to a set of permanents needs a controlledBy or "
                    + "otherPermanentsSharing subject: " + subject);
        }
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage) {
        this(damage, false);
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage, boolean damagedCreaturesMustAttackThisTurn) {
        this(new Fixed(damage), Subjects.controlledBy(TARGETED_PLAYER, new PermanentIsCreaturePredicate()),
                damagedCreaturesMustAttackThisTurn);
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage, PermanentSubject subject) {
        this(new Fixed(damage), subject);
    }

    public DealDamageToPermanentsTargetControlsEffect(DynamicAmount damage, PermanentSubject subject) {
        this(damage, subject, false);
    }

    /** {@code damage} to each permanent matching {@code filter} that the targeted player controls. */
    public static DealDamageToPermanentsTargetControlsEffect toTargetedPlayers(
            int damage, PermanentPredicate filter) {
        return toTargetedPlayers(new Fixed(damage), filter);
    }

    /**
     * {@code damage} to each permanent matching {@code filter} that the targeted player, or the targeted planeswalker's
     * controller, controls.
     */
    public static DealDamageToPermanentsTargetControlsEffect toTargetedPlayers(
            DynamicAmount damage, PermanentPredicate filter) {
        return new DealDamageToPermanentsTargetControlsEffect(damage, Subjects.controlledBy(TARGETED_PLAYER, filter));
    }

    /**
     * "… to target player or planeswalker and each creature that player or that planeswalker's controller controls"
     * (Flame Wave): {@code damage} to each such creature but the targeted planeswalker itself, which the target's own
     * damage effect beside this one deals to, so an animated planeswalker is dealt the damage once.
     */
    public static DealDamageToPermanentsTargetControlsEffect toCreaturesOfTargetedPlayer(DynamicAmount damage) {
        return toTargetedPlayers(damage, new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTargetCardPredicate()))));
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, true, null, false, 1);
    }
}
