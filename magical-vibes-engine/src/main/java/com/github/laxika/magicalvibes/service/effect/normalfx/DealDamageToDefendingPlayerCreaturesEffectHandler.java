package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDefendingPlayerCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToDefendingPlayerCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToDefendingPlayerCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToDefendingPlayerCreaturesEffect) effect;
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return;
        }
        UUID defendingPlayerId = gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (defendingPlayerId == null) {
            return;
        }

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        String cardName = entry.getCard().getName();
        FilterContext filterContext = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
        List<Permanent> battlefield = gameData.playerBattlefields.get(defendingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (e.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    continue;
                }
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.hasProtectionFromSource(gameData, permanent, entry.getCard())) {
                    gameLogService.append(gameData,
                            GameLog.textCardText(cardName + "'s damage to ", permanent.getCard(), " is prevented."));
                    continue;
                }
                damageSupport.dealCreatureDamage(gameData, entry, permanent, rawDamage);
            }
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
