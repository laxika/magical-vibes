package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves non-targeted damage dealt by an Aura to its enchanted creature. */
@Component
@RequiredArgsConstructor
public class DealDamageToEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEnchantedCreatureEffect) effect;
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchantedCreature == null || !gameQueryService.isCreature(gameData, enchantedCreature)) {
            return;
        }

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, aura));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
        damageSupport.dealCreatureDamage(gameData, entry, enchantedCreature, rawDamage, aura);
    }
}
