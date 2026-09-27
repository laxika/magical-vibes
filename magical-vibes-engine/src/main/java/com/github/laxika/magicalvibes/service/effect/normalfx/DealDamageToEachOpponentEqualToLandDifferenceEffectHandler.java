package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToLandDifferenceEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Spiteful Repossession's land-surplus damage and records its actual total. */
@Component
@RequiredArgsConstructor
public class DealDamageToEachOpponentEqualToLandDifferenceEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOpponentEqualToLandDifferenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            entry.setEventValue(0);
            return;
        }

        int controllerLandCount = countLands(gameData, controllerId);
        UUID damageSourceId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId() : entry.getCard().getId();
        int damageBefore = gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }

            int damage = countLands(gameData, playerId) - controllerLandCount;
            if (damage <= 0 || gameQueryService.isDamageFromStackEntryPrevented(gameData, entry)) {
                continue;
            }

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
        }

        entry.setEventValue(gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0)
                - damageBefore);
        gameOutcomeService.checkWinCondition(gameData);
    }

    private int countLands(GameData gameData, UUID playerId) {
        int count = 0;
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (gameQueryService.isLand(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }
}
