package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToEachOpponentEqualToControlledPermanentCountEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOpponentEqualToControlledPermanentCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        var countEffect = (DealDamageToEachOpponentEqualToControlledPermanentCountEffect) effect;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }

            int matchingPermanentCount = countMatchingPermanents(gameData, playerId, countEffect.filter());
            if (matchingPermanentCount == 0
                    || gameQueryService.isDamageFromStackEntryPrevented(gameData, entry)) {
                continue;
            }

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, matchingPermanentCount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    private int countMatchingPermanents(GameData gameData, UUID playerId,
                                        PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                count++;
            }
        }
        return count;
    }
}
