package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A targeted creature, or tokens created during this resolution, deal the specified damage.
 * Each permanent is its own damage source. The recipient is its controller by default;
 * {@link DamageRecipient#CONTROLLER} instead uses the resolving ability's controller.
 *
 * @param damage the amount each permanent deals, independent of its power
 * @param scope a targeted creature or the tokens created during this resolution
 * @param recipient the permanent's controller or the resolving ability's controller
 */
public record TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage, GrantScope scope,
                                                           DamageRecipient recipient)
        implements DamageDealingEffect {

    public TargetCreatureDealsDamageToControllerEffect {
        if (scope != GrantScope.TARGET && scope != GrantScope.TOKENS_CREATED_THIS_RESOLUTION) {
            throw new IllegalArgumentException("Unsupported damage source scope");
        }
        if (recipient != DamageRecipient.TARGET_PERMANENT_CONTROLLER && recipient != DamageRecipient.CONTROLLER) {
            throw new IllegalArgumentException("Unsupported damage recipient");
        }
    }

    public TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage) {
        this(damage, GrantScope.TARGET, DamageRecipient.TARGET_PERMANENT_CONTROLLER);
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
