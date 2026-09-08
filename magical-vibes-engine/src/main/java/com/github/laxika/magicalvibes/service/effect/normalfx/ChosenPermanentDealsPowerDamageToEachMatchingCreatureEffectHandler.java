package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenPermanentDealsPowerDamageToEachMatchingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChosenPermanentDealsPowerDamageToEachMatchingCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChosenPermanentDealsPowerDamageToEachMatchingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChosenPermanentDealsPowerDamageToEachMatchingCreatureEffect) effect;
        Permanent source = entry.getChosenPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
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
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(source.getCard().getId())
                .withSourceControllerId(gameQueryService.findPermanentController(gameData, source.getId()));

        List<Permanent> targets = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && (e.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(
                        permanent, e.filter(), filterContext))) {
                    targets.add(permanent);
                }
            }
        }

        for (Permanent target : targets) {
            damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage, source);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
