package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A targeted creature, or tokens created during this resolution, deal the specified damage.
 * Each permanent is its own damage source. The player dealt the damage is that permanent's controller
 * ({@code controllerOf(target())}, the default) or the resolving ability's controller ({@code you()}).
 *
 * @param damage the amount each permanent deals, independent of its power
 * @param scope a targeted creature or the tokens created during this resolution
 * @param player {@code controllerOf(target())}: each damaging permanent's own controller; {@code you()}: the resolving
 *               ability's controller
 */
public record TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage, GrantScope scope,
                                                           PlayerSubject player)
        implements DamageDealingEffect {

    public TargetCreatureDealsDamageToControllerEffect {
        if (scope != GrantScope.TARGET && scope != GrantScope.TOKENS_CREATED_THIS_RESOLUTION) {
            throw new IllegalArgumentException("Unsupported damage source scope");
        }
        if (!(player instanceof PlayerSubject.You) && !player.equals(Subjects.controllerOf(Subjects.target()))) {
            throw new IllegalArgumentException("Unsupported damage recipient: " + player);
        }
    }

    public TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage) {
        this(damage, GrantScope.TARGET, Subjects.controllerOf(Subjects.target()));
    }

    public TargetCreatureDealsDamageToControllerEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.harmful(TargetPredicates.creature()) : TargetSpec.NONE;
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
}
