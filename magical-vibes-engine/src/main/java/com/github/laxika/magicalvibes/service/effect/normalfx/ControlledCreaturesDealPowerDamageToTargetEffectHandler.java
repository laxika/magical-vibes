package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealPowerDamageToTargetEffect;
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
public class ControlledCreaturesDealPowerDamageToTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlledCreaturesDealPowerDamageToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ControlledCreaturesDealPowerDamageToTargetEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(e);
        UUID targetId = targetIds.isEmpty() ? entry.getTargetId() : targetIds.getFirst();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        FilterContext ctx = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
        List<Permanent> hunters = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), ctx)) {
                hunters.add(p);
            }
        }
        if (hunters.isEmpty()) {
            return;
        }

        for (Permanent hunter : hunters) {
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.isPreventedFromDealingDamage(gameData, hunter)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(hunter.getCard(), "'s damage is prevented."));
                continue;
            }
            if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, target, hunter)) {
                gameLogService.append(gameData,
                        GameLog.cardTextCard(hunter.getCard(), "'s damage to ", target.getCard(), " is prevented."));
                continue;
            }

            int power = gameQueryService.getPowerBasedDamage(gameData, hunter);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
            damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage, hunter);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
