package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.UUID;

/**
 * A spell may be accepted by any player who chooses to take damage; if all players decline, its
 * targeted player draws cards instead. The card-definition constructor leaves the resolution
 * state unset; the normal and may handlers carry the APNAP queue between player choices.
 *
 * @param damage             damage dealt to the first player who accepts
 * @param drawCount          cards drawn by the targeted player if everyone declines
 * @param targetPlayerId     the spell's target player
 * @param remainingPlayerIds players still eligible to accept the damage
 * @param sourceControllerId controller of the spell, preserved while the accepting player chooses
 */
public record AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect(
        int damage,
        int drawCount,
        UUID targetPlayerId,
        List<UUID> remainingPlayerIds,
        UUID sourceControllerId
) implements DamageDealingEffect {

    public AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect(int damage, int drawCount) {
        this(damage, drawCount, null, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
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
