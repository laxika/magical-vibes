package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One permanent (the {@code dealer}) deals damage equal to a multiple of its power to another permanent (the
 * {@code recipient}): a one-sided fight ("target creature you control deals damage equal to its power to target
 * creature you don't control", Wing Puncture). The dealer is the damage source: its power, colour (protection),
 * "can't deal damage" restrictions, deathtouch, lifelink, infect, wither and damage triggers key off it, not the
 * spell. Not a fight (CR 701.14a): the recipient deals no damage back.
 *
 * <p>Shapes (anything else is rejected):</p>
 * <ul>
 *   <li>{@code (target(g), target(g'))}: the bite of two target groups ({@code g}/{@code g'} are indices into the
 *       card's {@code target(...)} declarations, or flat target positions for an activated ability, see
 *       {@code StackEntry.targetsForGroup}; default 0 and 1). The card declares the targets. Nothing happens if
 *       either is gone.</li>
 *   <li>{@code (target(), target())}: "target creature deals damage equal to its power to itself" (Justice
 *       Strike).</li>
 *   <li>{@code (target(), source())}: "each of those creatures deals damage equal to its power to this creature"
 *       (Living Inferno); every target of the effect deals its damage, the ability's filter declares them.</li>
 *   <li>{@code (source(), target())}: "this creature deals damage equal to its power to target creature" (Goblin
 *       Fireleaper); a source that left uses its last-known power (CR 608.2h).</li>
 *   <li>{@code (attachedAtTrigger(), target())}: "you may have it deal damage equal to its power to target creature"
 *       after "whenever enchanted creature becomes blocked" (Laccolith Rig): the creature the Aura enchanted as the
 *       ability triggered, as it last existed if it left.</li>
 *   <li>{@code (triggering(), target())}: "whenever equipped creature attacks, it deals damage equal to its power to
 *       target creature" (Lothlórien Blade): the creature the trigger event was about, with its power as the
 *       ability triggered if it left.</li>
 * </ul>
 * A dealer named by a target that is no longer a creature deals no damage: a noncreature permanent has no power
 * (CR 208.3), and a source that would deal 0 damage deals none (CR 120.8).
 *
 * @param dealer           the permanent that deals the damage
 * @param recipient        the permanent dealt the damage
 * @param powerMultiplier  the multiple of the dealer's power dealt ("twice its power")
 * @param recordExcessDamage the bite records the excess damage over lethal for a following
 *                         {@link com.github.laxika.magicalvibes.model.amount.EventValue} (two target groups only)
 * @param targetFilter     an extra restriction on a single {@code target()} recipient ("creature an opponent
 *                         controls"), or {@code null}
 */
public record TargetDealsPowerDamageToTargetEffect(PermanentSubject dealer,
                                                   PermanentSubject recipient,
                                                   int powerMultiplier,
                                                   boolean recordExcessDamage,
                                                   PermanentPredicate targetFilter) implements DamageDealingEffect {

    public TargetDealsPowerDamageToTargetEffect {
        if (powerMultiplier < 1) {
            throw new IllegalArgumentException("Power multiplier must be positive");
        }
        boolean groups = isGroupTarget(dealer) && isGroupTarget(recipient);
        boolean singleTargetRecipient = recipient instanceof PermanentSubject.Target r && r.group() == null;
        boolean supported = groups
                || singleTargetRecipient && (dealer.equals(recipient)
                        || dealer instanceof PermanentSubject.Source
                        || dealer instanceof PermanentSubject.AttachedAtTrigger
                        || dealer instanceof PermanentSubject.Triggering)
                || dealer instanceof PermanentSubject.Target d && d.group() == null
                        && recipient instanceof PermanentSubject.Source;
        if (!supported) {
            throw new IllegalArgumentException("Unsupported dealer and recipient: " + dealer + ", " + recipient);
        }
        if (recordExcessDamage && !groups) {
            throw new IllegalArgumentException("Excess damage is recorded only by the two-target-group bite");
        }
        if (targetFilter != null && !singleTargetRecipient) {
            throw new IllegalArgumentException("A target filter needs a single target() recipient");
        }
    }

    public TargetDealsPowerDamageToTargetEffect(int sourceTargetGroup, int victimTargetGroup,
                                                int powerMultiplier, boolean recordExcessDamage) {
        this(Subjects.target(sourceTargetGroup), Subjects.target(victimTargetGroup), powerMultiplier,
                recordExcessDamage, null);
    }

    public TargetDealsPowerDamageToTargetEffect(int sourceTargetGroup, int victimTargetGroup) {
        this(sourceTargetGroup, victimTargetGroup, 1, false);
    }

    /** "Target creature deals damage equal to its power to another target" — groups 0 and 1. */
    public TargetDealsPowerDamageToTargetEffect() {
        this(0, 1, 1, false);
    }

    public TargetDealsPowerDamageToTargetEffect(int powerMultiplier) {
        this(0, 1, powerMultiplier, false);
    }

    public TargetDealsPowerDamageToTargetEffect(int sourceTargetGroup, int victimTargetGroup,
                                                int powerMultiplier) {
        this(sourceTargetGroup, victimTargetGroup, powerMultiplier, false);
    }

    /** {@code dealer} deals damage equal to its power to {@code recipient}. */
    public TargetDealsPowerDamageToTargetEffect(PermanentSubject dealer, PermanentSubject recipient) {
        this(dealer, recipient, 1, false, null);
    }

    /** Bite variant that records excess damage for a following {@link com.github.laxika.magicalvibes.model.amount.EventValue}. */
    public static TargetDealsPowerDamageToTargetEffect recordingExcessDamage() {
        return new TargetDealsPowerDamageToTargetEffect(0, 1, 1, true);
    }

    /** "Target creature deals damage to itself equal to {@code powerMultiplier} times its power." */
    public static TargetDealsPowerDamageToTargetEffect toItself(int powerMultiplier) {
        return new TargetDealsPowerDamageToTargetEffect(Subjects.target(), Subjects.target(), powerMultiplier,
                false, null);
    }

    /** "Target creature deals damage equal to its power to itself." */
    public static TargetDealsPowerDamageToTargetEffect toItself() {
        return toItself(1);
    }

    /** "This creature deals damage equal to its power to target creature [matching {@code targetFilter}]." */
    public static TargetDealsPowerDamageToTargetEffect bySource(PermanentPredicate targetFilter) {
        return new TargetDealsPowerDamageToTargetEffect(Subjects.source(), Subjects.target(), 1, false,
                targetFilter);
    }

    /** "This creature deals damage equal to its power to target creature." */
    public static TargetDealsPowerDamageToTargetEffect bySource() {
        return bySource(null);
    }

    private static boolean isGroupTarget(PermanentSubject subject) {
        return subject instanceof PermanentSubject.Target target && target.group() != null;
    }

    /** Whether the two target groups of the bite name the dealer and the recipient. */
    public boolean readsTwoTargetGroups() {
        return isGroupTarget(dealer) && isGroupTarget(recipient);
    }

    /**
     * The bite of two target groups declares a benign spec over any player or permanent: its groups' filters say what
     * each target may be (the dealer and the recipient differ), and protection stops either target whatever the spec's
     * polarity (CR 702.16b). The polarity is read only by the AI's activated-ability ranking, which must not rank the
     * dealer, the controller's own creature, as an opposing target.
     */
    @Override
    public TargetSpec targetSpec() {
        if (readsTwoTargetGroups()) {
            return recordExcessDamage
                    ? TargetSpec.harmful(TargetPredicates.creature())
                    : TargetSpec.benign(TargetPredicates.playerOrPermanent());
        }
        if (recipient instanceof PermanentSubject.Target) {
            return targetFilter == null
                    ? TargetSpec.harmful(TargetPredicates.creature())
                    : TargetSpec.harmful(TargetPredicates.creature(), targetFilter);
        }
        return TargetSpec.NONE;
    }

    @Override
    public DynamicAmount damageAmount() {
        return new SourcePower();
    }

    /**
     * True for a dealer that is not a target (this creature, the triggering creature): the AI scores those as
     * creature damage from a permanent it already has, as it did for the old self-dealing records.
     */
    @Override
    public boolean canDamageCreatures() {
        return dealer instanceof PermanentSubject.Source || dealer instanceof PermanentSubject.Triggering;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
