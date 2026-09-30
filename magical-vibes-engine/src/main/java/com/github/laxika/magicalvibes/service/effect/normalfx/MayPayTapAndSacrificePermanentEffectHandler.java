package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.ability.cost.TapCostSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Expands the combined tap-and-sacrifice payment into the standard may-pay flow. */
@Component
@RequiredArgsConstructor
public class MayPayTapAndSacrificePermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TapCostSupport tapCostSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayTapAndSacrificePermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayPayTapAndSacrificePermanentEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());

        var battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        boolean canSacrifice = battlefield != null && battlefield.stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, e.sacrificeFilter(), filterContext));
        int requiredTaps = tapCostSupport.requiredCount(
                gameData, e.tapCost(), entry.getSourcePermanentId(), entry.getXValue());
        boolean canTap = battlefield != null && battlefield.stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> !e.tapCost().excludeSource()
                        || !permanent.getId().equals(entry.getSourcePermanentId()))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, e.tapCost().filter(), filterContext))
                .count() >= requiredTaps;

        if (!canSacrifice || !canTap) {
            if (!canSacrifice) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(entry.getControllerId()) + " has no "
                                + e.permanentDescription() + " to sacrifice."));
            }
            return;
        }

        int effectIndex = entry.getResolvingEffectIndex();
        if (effectIndex < 0) {
            effectIndex = entry.getEffectsToResolve().indexOf(effect);
        }
        if (effectIndex < 0) {
            throw new IllegalStateException("Combined payment effect is not part of the resolving stack entry");
        }

        entry.insertEffectsToResolve(effectIndex + 1, List.of(new MayPayTapPermanentsEffect(
                e.tapCost(),
                new SacrificePermanentThenEffect(
                        e.sacrificeFilter(), e.thenEffect(), e.permanentDescription()),
                e.prompt())));
    }
}
