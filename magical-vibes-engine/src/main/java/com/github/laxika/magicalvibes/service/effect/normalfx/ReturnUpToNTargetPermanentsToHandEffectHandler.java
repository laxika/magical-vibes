package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNTargetPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Return up to N target permanents to their owners' hands." Gathers every permanent on the
 * battlefield and prompts the controller to choose up to N to return (they may choose none).
 * Completion is handled by {@link MultiPermanentChoiceContext.ReturnTargetPermanentsToHand}.
 */
@Component
@RequiredArgsConstructor
public class ReturnUpToNTargetPermanentsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnUpToNTargetPermanentsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnUpToNTargetPermanentsToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID chooserId = controllerId;

        List<UUID> permanentIds = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());
        if (e.opponentChooses()) {
            UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
            if (opponentId == null) {
                return;
            }
            chooserId = opponentId;
            List<com.github.laxika.magicalvibes.model.Permanent> battlefield =
                    gameData.playerBattlefields.getOrDefault(opponentId, List.of());
            for (var permanent : battlefield) {
                if (e.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                        permanent, e.filter(), filterContext)) {
                    permanentIds.add(permanent.getId());
                }
            }
        } else {
            gameData.forEachPermanent((pid, permanent) -> {
                if (e.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                        permanent, e.filter(), filterContext)) {
                    permanentIds.add(permanent.getId());
                }
            });
        }

        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no permanents to return."));
            return;
        }

        int maxCount = Math.min(e.maxCount(), permanentIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, chooserId, permanentIds, maxCount,
                new MultiPermanentChoiceContext.ReturnTargetPermanentsToHand(e.thenEffect()),
                "Choose up to " + maxCount + " permanent" + (maxCount == 1 ? "" : "s")
                        + " to return to their owners' hands.");
    }
}
