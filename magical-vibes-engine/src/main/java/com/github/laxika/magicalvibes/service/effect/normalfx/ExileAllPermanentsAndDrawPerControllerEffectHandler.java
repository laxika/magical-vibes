package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsAndDrawPerControllerEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllPermanentsAndDrawPerControllerEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllPermanentsAndDrawPerControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileAllPermanentsAndDrawPerControllerEffect) effect;
        List<Permanent> toExile = new ArrayList<>();
        Map<UUID, Integer> exiledByController = new HashMap<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withXValue(entry.getXValue());

        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    toExile.add(permanent);
                    exiledByController.merge(controllerId, 1, Integer::sum);
                }
            }
        });

        for (Permanent permanent : toExile) {
            permanentRemovalService.removePermanentToExile(gameData, permanent);
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        for (UUID playerId : gameData.orderedPlayerIds) {
            playerInteractionSupport.applyDrawCards(
                    gameData, playerId, exiledByController.getOrDefault(playerId, 0));
        }
    }
}
