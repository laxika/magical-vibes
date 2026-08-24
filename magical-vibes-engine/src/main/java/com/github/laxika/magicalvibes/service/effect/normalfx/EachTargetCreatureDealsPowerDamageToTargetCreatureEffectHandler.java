package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachTargetCreatureDealsPowerDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetCreatureDealsPowerDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachTargetCreatureDealsPowerDamageToTargetCreatureEffect) effect;
        List<UUID> sourceIds = entry.targetsForGroup(e.sourceTargetGroup());
        List<UUID> victimIds = entry.targetsForGroup(e.victimTargetGroup());
        if (sourceIds.isEmpty() || victimIds.isEmpty()) {
            return;
        }

        Permanent victim = gameQueryService.findPermanentById(gameData, victimIds.getFirst());
        if (victim == null || !gameQueryService.isCreature(gameData, victim)) {
            return;
        }

        for (UUID sourceId : sourceIds) {
            Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
            if (source == null || !gameQueryService.isCreature(gameData, source)) {
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, victim, source)) {
                gameLogService.append(gameData, GameLog.cardTextCard(source.getCard(), "'s damage to ", victim.getCard(), " is prevented."));
                continue;
            }

            int power = gameQueryService.getPowerBasedDamage(gameData, source);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
            damageSupport.dealCreatureDamage(gameData, entry, victim, rawDamage, source);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
