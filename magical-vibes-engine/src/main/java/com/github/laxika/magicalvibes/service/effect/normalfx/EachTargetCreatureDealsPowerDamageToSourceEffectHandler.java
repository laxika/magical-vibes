package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachTargetCreatureDealsPowerDamageToSourceEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetCreatureDealsPowerDamageToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent recipient = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (recipient == null) {
            return;
        }

        for (UUID targetId : entry.targetsForEffect(effect)) {
            Permanent source = gameQueryService.findPermanentById(gameData, targetId);
            if (source == null || !gameQueryService.isCreature(gameData, source)) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            if (controllerId == null) {
                continue;
            }

            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
                continue;
            }

            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, recipient, source)) {
                gameLogService.append(gameData, GameLog.textCardText(source.getCard().getName()
                        + "'s damage to ", recipient.getCard(), " is prevented."));
                continue;
            }

            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s ability",
                    List.of(),
                    null,
                    source.getId());
            int power = gameQueryService.getPowerBasedDamage(gameData, source);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
            damageSupport.dealCreatureDamage(gameData, damageEntry, recipient, rawDamage, source);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
