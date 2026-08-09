package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EnchantedCreatureDealsPowerDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureDealsPowerDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = entry.getSourcePermanentSnapshot() != null
                && entry.getSourcePermanentSnapshot().getCard().isAura()
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (creature == null || target == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, creature)) {
            gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s damage is prevented."));
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.hasProtectionFromSource(gameData, target, creature)) {
            gameLogService.append(gameData,
                    GameLog.cardTextCard(creature.getCard(), "'s damage to ", target.getCard(), " is prevented."));
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) {
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, creature);
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                creature.getCard(),
                controllerId,
                creature.getCard().getName() + "'s ability",
                List.of(),
                target.getId(),
                creature.getId());
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
        damageSupport.dealCreatureDamage(gameData, damageEntry, target, rawDamage, creature);
    }
}
