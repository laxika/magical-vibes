package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.UUID;

/**
 * ETB punisher (Vexing Devil): any opponent may have the source deal {@code damage} to them. If any
 * player does, sacrifice the source. Opponents choose in turn order (next opponent first; if an
 * opponent is active, that opponent first). Accepting deals the damage from the source; the source
 * is sacrificed even if the damage is prevented or redirected. The no-arg-ish {@code (damage)}
 * constructor is used in the card definition; resolution stamps the remaining-opponent queue and
 * source ids onto subsequent instances carried by the may-ability prompts.
 *
 * @param damage               how much damage an accepting opponent is dealt by the source
 * @param remainingOpponentIds opponents still to choose (null in the card definition)
 * @param abilityControllerId  controller of the triggered ability (null in the card definition)
 * @param sourcePermanentId    the permanent to sacrifice if anyone accepts (null in the card definition)
 * @param anyAccepted          whether any opponent has already accepted during this resolution
 */
public record AnyOpponentMayTakeDamageSacrificeSourceEffect(
        int damage,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        boolean anyAccepted
) implements DamageDealingEffect {

    public AnyOpponentMayTakeDamageSacrificeSourceEffect(int damage) {
        this(damage, null, null, null, false);
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
}
