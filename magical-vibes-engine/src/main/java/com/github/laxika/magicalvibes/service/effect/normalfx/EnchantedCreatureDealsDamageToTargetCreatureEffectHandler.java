package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureDealsDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedCreatureDealsDamageToTargetCreatureEffect) effect;
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) return;

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchantedCreature == null || !gameQueryService.isCreature(gameData, enchantedCreature)) return;

        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, enchantedCreature));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;
        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.isPreventedFromDealingDamage(gameData, enchantedCreature)
                || gameQueryService.hasProtectionFromDamageSource(gameData, target, enchantedCreature))) {
            gameLogService.append(gameData, GameLog.cardThen(enchantedCreature.getCard(), "'s damage is prevented."));
            return;
        }
        damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage, enchantedCreature);
    }
}
