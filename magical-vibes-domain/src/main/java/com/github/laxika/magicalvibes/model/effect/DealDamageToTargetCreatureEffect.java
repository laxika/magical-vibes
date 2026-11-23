package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to target creature, or to the one permanent {@code subject} names. The amount is a
 * {@link DynamicAmount} evaluated at resolution (fixed number, X paid, source's toughness, controlled permanent count,
 * …).
 *
 * <p>Subjects other than {@link Subjects#target()} target nothing, so protection's targeting half and hexproof don't
 * stop them:
 * <ul>
 *   <li>{@link Subjects#source()}: "and X damage to itself" (Sunflare Shaman, Psionic Sliver), dealt to the live source
 *       permanent only;</li>
 *   <li>{@link Subjects#triggering()}: "deals 4 damage to it" (Raking Canopy, Aether Flash), the permanent the trigger
 *       event was about if it is still a creature, planeswalker or battle;</li>
 *   <li>{@link Subjects#attached()}: an Aura's "deals 2 damage to enchanted creature" (Fatal Attraction, Tilonalli's
 *       Crown), the creature it enchants as the ability resolves;</li>
 *   <li>{@link Subjects#attachedAtTrigger()}: an Aura's "deals that much damage to that creature" after "whenever
 *       enchanted creature deals damage" (Guilty Conscience), the creature it enchanted as the ability triggered;</li>
 *   <li>{@link Subjects#thisWay}: "deals 2 damage to [the creature chosen]" after a
 *       {@code ChoosePermanentsEffect} step (Wicked Guardian), each creature that step named, as it is now.</li>
 * </ul>
 * The two Aura subjects have the Aura, as it is or last existed, deal the damage.
 *
 * @param damage          the amount of damage to deal
 * @param unpreventable   when true, the damage can't be prevented (e.g. Combust); target only
 * @param targetPredicate optional extra restriction on the targeted creature (for example,
 *                        "target creature an opponent controls" or "target Spirit creature");
 *                        {@code null} for plain "target creature"; target only
 * @param damageSource    what deals the damage: the ability's own source, or the permanent the trigger event was
 *                        about ("have it deal damage", Farrel's Mantle's enchanted creature, CR 608.2h); the trigger
 *                        event's permanent only with a target
 * @param subject         which permanent is dealt the damage; {@code null} means {@link Subjects#target()}
 */
public record DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable,
                                               PermanentPredicate targetPredicate, DamageSource damageSource,
                                               PermanentSubject subject)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureEffect {
        subject = subject == null ? Subjects.target() : subject;
        if (!subject.equals(Subjects.target())) {
            if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Triggering
                    || subject instanceof PermanentSubject.Attached
                    || subject instanceof PermanentSubject.AttachedAtTrigger
                    || subject instanceof PermanentSubject.ThisWay)) {
                throw new IllegalArgumentException("Unsupported damage subject: " + subject);
            }
            if (unpreventable || targetPredicate != null || damageSource != DamageSource.ABILITY_SOURCE) {
                throw new IllegalArgumentException("Only a target subject takes a target filter, unpreventable "
                        + "damage or another damage source: " + subject);
            }
        }
    }

    public DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable,
                                            PermanentPredicate targetPredicate, DamageSource damageSource) {
        this(damage, unpreventable, targetPredicate, damageSource, Subjects.target());
    }

    public DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable,
                                            PermanentPredicate targetPredicate) {
        this(damage, unpreventable, targetPredicate, DamageSource.ABILITY_SOURCE);
    }

    /** {@code damage} to the permanent {@code subject} names. */
    public DealDamageToTargetCreatureEffect(PermanentSubject subject, DynamicAmount damage) {
        this(damage, false, null, DamageSource.ABILITY_SOURCE, subject);
    }

    /** {@code damage} to the permanent {@code subject} names. */
    public DealDamageToTargetCreatureEffect(PermanentSubject subject, int damage) {
        this(subject, new Fixed(damage));
    }

    /** {@code damage} to target creature matching {@code targetPredicate}, dealt by the trigger event's permanent. */
    public static DealDamageToTargetCreatureEffect byTriggeringPermanent(DynamicAmount damage,
                                                                         PermanentPredicate targetPredicate) {
        return new DealDamageToTargetCreatureEffect(damage, false, targetPredicate, DamageSource.TRIGGERING_PERMANENT);
    }

    public DealDamageToTargetCreatureEffect(int damage) {
        this(new Fixed(damage), false, null);
    }

    public DealDamageToTargetCreatureEffect(int damage, boolean unpreventable) {
        this(new Fixed(damage), unpreventable, null);
    }

    public DealDamageToTargetCreatureEffect(DynamicAmount damage) {
        this(damage, false, null);
    }

    public DealDamageToTargetCreatureEffect(DynamicAmount damage, boolean unpreventable) {
        this(damage, unpreventable, null);
    }

    public DealDamageToTargetCreatureEffect(int damage, PermanentPredicate targetPredicate) {
        this(new Fixed(damage), false, targetPredicate);
    }

    /** The same effect dealing {@code newDamage} instead. */
    public DealDamageToTargetCreatureEffect withDamage(DynamicAmount newDamage) {
        return new DealDamageToTargetCreatureEffect(newDamage, unpreventable, targetPredicate, damageSource, subject);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget()
                ? TargetSpec.harmful(TargetPredicates.creature(), targetPredicate)
                : TargetSpec.NONE;
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    /** A permanent damaging itself is no damage it can aim at a creature, so {@link Subjects#source()} answers no. */
    @Override
    public boolean canDamageCreatures() {
        return !(subject instanceof PermanentSubject.Source);
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
