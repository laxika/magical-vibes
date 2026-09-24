package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutUpToNControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a non-targeting choice of up to N controlled permanents to phase out. */
@Component
@RequiredArgsConstructor
public class PhaseOutUpToNControlledPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutUpToNControlledPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PhaseOutUpToNControlledPermanentsEffect) effect;
        List<UUID> validIds = matchingControlledPermanentIds(gameData, entry, e);
        int maxCount = Math.min(Math.max(0, amountEvaluationService.evaluate(
                gameData, e.maxCount(), AmountContext.forStackEntry(entry, null))), validIds.size());
        if (maxCount == 0) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData, entry.getControllerId(), validIds, maxCount,
                new MultiPermanentChoiceContext.PhaseOutUpToNControlledPermanents(e),
                "Choose up to " + maxCount + " permanents you control to phase out.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.PhaseOutUpToNControlledPermanents context,
                               StackEntry entry) {
        List<UUID> validIds = matchingControlledPermanentIds(gameData, entry, context.effect());
        List<Permanent> toPhaseOut = permanentIds.stream()
                .filter(validIds::contains)
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(java.util.Objects::nonNull)
                .toList();
        if (toPhaseOut.isEmpty()) {
            return;
        }

        phasingService.phaseOut(gameData, toPhaseOut);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" phases out " + toPhaseOut.size() + " permanent(s).")
                .build());
    }

    private List<UUID> matchingControlledPermanentIds(GameData gameData, StackEntry entry,
                                                       PhaseOutUpToNControlledPermanentsEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> validIds = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (effect.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                    permanent, effect.filter(), filterContext)) {
                validIds.add(permanent.getId());
            }
        }
        return validIds;
    }
}
