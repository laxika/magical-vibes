package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachBlockingCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link FlipCoinForEachBlockingCreatureEffect}, making one independent flip for each
 * blocking creature currently on the battlefield.
 */
@Component
@RequiredArgsConstructor
public class FlipCoinForEachBlockingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinForEachBlockingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> blockers = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(Permanent::isBlocking)
                .forEach(blockers::add));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        String cardName = entry.getCard().getName();
        for (Permanent blocker : blockers) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, entry.getControllerId());
            boolean wonFlip = result.heads();
            String outcome = wonFlip ? " wins" : " loses";
            gameLogService.append(gameData, GameLog.text(playerName + outcome
                    + " the coin flip for " + cardName + " for " + blocker.getCard().getName()
                    + coinFlipService.replacementDetails(result) + "."));

            if (wonFlip) {
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, entry.getControllerId());
                gameData.creaturesPreventedFromDealingCombatDamage.add(blocker.getId());
                gameLogService.append(gameData,
                        GameLog.cardThen(blocker.getCard(), " will deal no combat damage this turn."));
            } else {
                triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, entry.getControllerId());
            }
        }
    }
}
