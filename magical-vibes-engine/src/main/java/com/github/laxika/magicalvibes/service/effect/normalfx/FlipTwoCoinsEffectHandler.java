package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
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
public class FlipTwoCoinsEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipTwoCoinsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FlipTwoCoinsEffect) effect;

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        boolean firstFlip = ThreadLocalRandom.current().nextBoolean();
        boolean secondFlip = ThreadLocalRandom.current().nextBoolean();

        String firstResult = firstFlip ? "heads" : "tails";
        String secondResult = secondFlip ? "heads" : "tails";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " flips two coins for " + sourceName + ": " + firstResult + " and " + secondResult + "."));

        CardEffect chosen;
        if (firstFlip && secondFlip) {
            chosen = e.bothHeads();
        } else if (!firstFlip && !secondFlip) {
            chosen = e.bothTails();
        } else {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(chosen);
        if (handler != null) {
            handler.resolve(gameData, entry, chosen);
        } else {
            log.warn("No handler for wrapped effect in FlipTwoCoinsEffect: {}",
                    chosen.getClass().getSimpleName());
        }
    
    }
}
