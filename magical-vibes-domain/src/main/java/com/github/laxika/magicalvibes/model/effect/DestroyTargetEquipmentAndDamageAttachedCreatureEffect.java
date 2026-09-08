package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Destroys target Equipment, then deals 2 damage to the creature it was attached to. */
public record DestroyTargetEquipmentAndDamageAttachedCreatureEffect()
        implements RemovalEffect, DamageDealingEffect {

    private static final DynamicAmount DAMAGE = new Fixed(2);
    private static final PermanentPredicate EQUIPMENT =
            new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT);

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), EQUIPMENT);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public DynamicAmount damageAmount() {
        return DAMAGE;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
