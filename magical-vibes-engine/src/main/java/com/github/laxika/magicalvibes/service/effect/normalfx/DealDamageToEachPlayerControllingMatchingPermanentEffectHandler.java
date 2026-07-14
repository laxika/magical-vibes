package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerControllingMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEachPlayerControllingMatchingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachPlayerControllingMatchingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachPlayerControllingMatchingPermanentEffect) effect;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        FilterContext ctx = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            boolean controlsMatch = battlefield.stream()
                    .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(p, e.predicate(), ctx));
            if (controlsMatch) {
                damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
