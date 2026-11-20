package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A targeted creature, or tokens created during this resolution, deal the specified damage to a player. Each
 * permanent is its own damage source (its lifelink, infect and damage triggers apply). The player dealt the damage is
 * that permanent's controller ({@code controllerOf(target())}, the default: "target creature deals damage equal to its
 * power to that creature's controller", with {@code new SourcePower()}), the resolving ability's controller
 * ({@code you()}: "target creature an opponent controls deals damage equal to its power to you"), or each opponent of
 * the ability's controller ({@code eachOpponent()}, target only: "target creature you control deals damage equal to
 * its power to each opponent").
 *
 * @param damage the amount each permanent deals, evaluated with that permanent as the amount's source
 *               ({@code SourcePower} is its power)
 * @param subject a targeted creature or the tokens created during this resolution
 * @param player {@code controllerOf(target())}: each damaging permanent's own controller; {@code you()}: the resolving
 *               ability's controller; {@code eachOpponent()}: each opponent of the resolving ability's controller
 */
public record TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage, PermanentSubject subject,
                                                           PlayerSubject player)
        implements DamageDealingEffect {

    public TargetCreatureDealsDamageToControllerEffect {
        if (!(subject instanceof PermanentSubject.Target) && !Subjects.createdThisResolution().equals(subject)) {
            throw new IllegalArgumentException("Unsupported damage source subject");
        }
        boolean eachOpponent = player.equals(Subjects.eachOpponent());
        if (!(player instanceof PlayerSubject.You) && !player.equals(Subjects.controllerOf(Subjects.target()))
                && !eachOpponent) {
            throw new IllegalArgumentException("Unsupported damage recipient: " + player);
        }
        if (eachOpponent && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("Each opponent is dealt damage only by a targeted creature");
        }
    }

    public TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage) {
        this(damage, Subjects.target(), Subjects.controllerOf(Subjects.target()));
    }

    public TargetCreatureDealsDamageToControllerEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.harmful(TargetPredicates.creature())
                : TargetSpec.NONE;
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean damagesController() {
        return player instanceof PlayerSubject.You;
    }
}
