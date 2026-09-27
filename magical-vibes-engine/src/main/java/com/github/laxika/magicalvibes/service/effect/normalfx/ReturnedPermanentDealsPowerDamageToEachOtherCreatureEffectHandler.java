package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedPermanentDealsPowerDamageToEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnedPermanentDealsPowerDamageToEachOtherCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnedPermanentDealsPowerDamageToEachOtherCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            returnedCardId = entry.getTargetCardIds().getFirst();
        }
        if (returnedCardId == null) {
            return;
        }

        Permanent source = findPermanentByCardId(gameData, returnedCardId);
        if (source == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, source);
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
        List<Permanent> targets = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!permanent.getId().equals(source.getId())
                        && gameQueryService.isCreature(gameData, permanent)) {
                    targets.add(permanent);
                }
            }
        }

        for (Permanent target : targets) {
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, target, source)) {
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
                continue;
            }
            damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage, source);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId()))) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
