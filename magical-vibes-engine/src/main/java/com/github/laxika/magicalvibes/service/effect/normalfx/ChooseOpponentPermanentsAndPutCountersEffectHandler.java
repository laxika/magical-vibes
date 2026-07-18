package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentPermanentsAndPutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseOpponentPermanentsAndPutCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentPermanentsAndPutCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseOpponentPermanentsAndPutCountersEffect) effect;
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);

        // Find all eligible permanents opponents control matching the filter
        List<UUID> eligibleIds = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                    eligibleIds.add(perm.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability finds no eligible permanents."));
            log.info("Game {} - {} ETB: no eligible permanents for counter placement", gameData.id, entry.getCard().getName());
            return;
        }

        if (eligibleIds.size() <= e.maxCount()) {
            // Auto-place counters on all eligible permanents
            permanentCounterSupport.placeCountersOnPermanents(gameData, entry, eligibleIds, e.counterType());
        } else {
            // Player must choose exactly maxCount
            playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds,
                    e.maxCount(), new MultiPermanentChoiceContext.AimCounterPlacement(),
                    "Choose " + e.maxCount() + " nonenchantment permanents to put aim counters on.");
        }
    }
}
