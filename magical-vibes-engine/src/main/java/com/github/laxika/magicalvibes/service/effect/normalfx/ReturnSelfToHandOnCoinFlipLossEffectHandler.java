package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReturnSelfToHandOnCoinFlipLossEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BounceSupport bounceSupport;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    public ReturnSelfToHandOnCoinFlipLossEffectHandler(GameLogService gameLogService,
                                                       BounceSupport bounceSupport) {
        this(gameLogService, bounceSupport, null, null);
    }

    public ReturnSelfToHandOnCoinFlipLossEffectHandler(GameLogService gameLogService,
                                                       BounceSupport bounceSupport,
                                                       CoinFlipService coinFlipService) {
        this(gameLogService, bounceSupport, coinFlipService, null);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSelfToHandOnCoinFlipLossEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        boolean wonFlip = result.heads();

        String flipLog = wonFlip
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + ".";
        gameLogService.append(gameData, GameLog.text(flipLog));

        if (wonFlip && triggerCollectionService != null) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
        } else if (!wonFlip && triggerCollectionService != null) {
            triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, controllerId);
        }

        if (wonFlip) {
            return;
        }

        bounceSupport.applyReturnSelfToHand(gameData, entry);
    }
}
