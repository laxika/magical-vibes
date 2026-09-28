package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ControlledCreaturesDealDamageToTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlledCreaturesDealDamageToTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typed = (ControlledCreaturesDealDamageToTargetPlayerEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null || !gameData.playerIds.contains(targetId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int evaluatedDamage = amountEvaluationService.evaluate(gameData, typed.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, evaluatedDamage, entry);
        FilterContext context = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
        List<Permanent> sources = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, typed.filter(), context)) {
                sources.add(permanent);
            }
        }

        for (Permanent sourcePermanent : sources) {
            if (gameQueryService.findPermanentById(gameData, sourcePermanent.getId()) == null) {
                continue;
            }
            UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanent.getId());
            if (sourceControllerId == null) {
                continue;
            }
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourcePermanent.getCard(),
                    sourceControllerId,
                    sourcePermanent.getCard().getName() + "'s ability",
                    List.of(), null, sourcePermanent.getId());
            damageSupport.dealDamageToPlayer(gameData, damageEntry, targetId, rawDamage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
