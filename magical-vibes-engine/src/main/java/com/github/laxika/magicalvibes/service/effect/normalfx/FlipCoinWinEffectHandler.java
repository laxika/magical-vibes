package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinWinEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinWinEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FlipCoinWinEffect) effect;

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        boolean wonFlip = ThreadLocalRandom.current().nextBoolean();

        String flipLog = wonFlip
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(flipLog));

        CardEffect branch = wonFlip ? e.wrapped() : e.lost();
        if (branch == null) {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(branch);
        if (handler != null) {
            handler.resolve(gameData, entry, branch);
        } else {
            log.warn("No handler for branch effect in FlipCoinWinEffect: {}",
                    branch.getClass().getSimpleName());
        }
    }
}
