package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachMatchingPermanentDestroyOnLossEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService.CoinFlipResult;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves independent coin flips for matching permanents and destroys the permanents that lose.
 */
@Component
@RequiredArgsConstructor
public class FlipCoinForEachMatchingPermanentDestroyOnLossEffectHandler implements NormalEffectHandlerBean {

    private final CoinFlipService coinFlipService;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinForEachMatchingPermanentDestroyOnLossEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var flipEffect = (FlipCoinForEachMatchingPermanentDestroyOnLossEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withXValue(entry.getXValue());

        List<Permanent> matchingPermanents = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, flipEffect.filter(), filterContext)) {
                    matchingPermanents.add(permanent);
                }
            }
        });

        List<Permanent> lostPermanents = new ArrayList<>();
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        String sourceName = entry.getCard().getName();
        for (Permanent permanent : matchingPermanents) {
            CoinFlipResult result = coinFlipService.flip(gameData, entry.getControllerId());
            String outcome = result.heads() ? "wins" : "loses";
            gameLogService.append(gameData, GameLog.text(playerName + " " + outcome
                    + " the coin flip for " + sourceName + " for " + permanent.getCard().getName()
                    + coinFlipService.replacementDetails(result) + "."));

            if (result.heads()) {
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, entry.getControllerId());
            } else {
                lostPermanents.add(permanent);
            }
        }

        destructionSupport.destroyBatchCollecting(gameData, lostPermanents, sourceName, false);
    }
}
