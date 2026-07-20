package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureFightsTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureFightsTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID auraId = entry.getSourcePermanentId();
        if (targetId == null || auraId == null) {
            return;
        }

        // The fighting creature is the one this Aura is attached to (the enchanted creature), not the
        // Aura permanent itself. Re-derive it from the attachment at resolution.
        Permanent aura = gameQueryService.findPermanentById(gameData, auraId);
        if (aura == null || aura.getAttachedTo() == null) {
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (enchanted == null || target == null) {
            return;
        }

        // Enchanted creature deals damage equal to its power to the target creature.
        int enchantedPower = gameQueryService.getPowerBasedDamage(gameData, enchanted);
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, enchanted)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(enchanted.getCard(), "'s damage is prevented."));
        } else if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, target, enchanted)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(target.getCard(), " has protection — damage from ", enchanted.getCard(), " prevented."));
        } else {
            int damage = gameQueryService.applyDamageMultiplier(gameData, enchantedPower, entry);
            damageSupport.dealCreatureDamage(gameData, entry, target, damage, enchanted);
        }

        // Target creature deals damage equal to its power back to the enchanted creature.
        int targetPower = gameQueryService.getPowerBasedDamage(gameData, target);
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, target)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), "'s damage is prevented."));
        } else if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, enchanted, target)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(enchanted.getCard(), " has protection — damage from ", target.getCard(), " prevented."));
        } else {
            int damage = gameQueryService.applyDamageMultiplier(gameData, targetPower, entry);
            damageSupport.dealCreatureDamage(gameData, entry, enchanted, damage, target);
        }
    }
}
