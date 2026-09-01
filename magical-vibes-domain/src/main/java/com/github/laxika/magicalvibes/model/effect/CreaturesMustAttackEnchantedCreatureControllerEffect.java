package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Static requirement for an Aura: every creature that can attack the enchanted creature's
 * controller must attack that player each combat if able.
 */
public record CreaturesMustAttackEnchantedCreatureControllerEffect()
        implements CombatAttackRequirementEffect {

    private static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return CREATURE;
    }

    @Override
    public boolean isActive(GameData gameData, Permanent sourcePermanent) {
        Permanent enchantedPermanent = findEnchantedPermanent(gameData, sourcePermanent);
        return enchantedPermanent != null && gameData.findControllerOf(enchantedPermanent) != null;
    }

    @Override
    public UUID requiredAttackTargetId(GameData gameData, Permanent sourcePermanent) {
        Permanent enchantedPermanent = findEnchantedPermanent(gameData, sourcePermanent);
        return enchantedPermanent == null ? null : gameData.findControllerOf(enchantedPermanent);
    }

    private Permanent findEnchantedPermanent(GameData gameData, Permanent sourcePermanent) {
        UUID attachedTo = sourcePermanent.getAttachedTo();
        if (attachedTo == null) {
            return null;
        }
        final Permanent[] enchanted = {null};
        gameData.forEachPermanent((ignored, permanent) -> {
            if (enchanted[0] == null && attachedTo.equals(permanent.getId())) {
                enchanted[0] = permanent;
            }
        });
        return enchanted[0];
    }
}
