package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.UUID;

/**
 * Players in turn order may have the source deal damage to themselves. The first player who
 * accepts is dealt the damage; if every player declines, the source gets the temporary boost.
 * Resolution stamps the remaining-player queue and source ids onto the prompted instances.
 */
public record AnyPlayerMayTakeDamageOrBoostSelfEffect(
        int damage,
        int powerBoost,
        int toughnessBoost,
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID sourcePermanentId
) implements DamageDealingEffect {

    public AnyPlayerMayTakeDamageOrBoostSelfEffect(int damage, int powerBoost, int toughnessBoost) {
        this(damage, powerBoost, toughnessBoost, null, null, null);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
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
        return true;
    }
}
