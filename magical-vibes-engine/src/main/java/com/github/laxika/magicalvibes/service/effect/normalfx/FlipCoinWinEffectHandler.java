package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
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

        dispatch(gameData, entry, branch);
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        // SequenceEffect has no handler of its own — expand it here so a multi-step branch resolves
        // each step in order against the same entry. Dispatch is synchronous, so sequence steps must
        // be synchronous (no async player-input pauses).
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler != null) {
            handler.resolve(gameData, entry, effect);
        } else {
            log.warn("No handler for branch effect in FlipCoinWinEffect: {}",
                    effect.getClass().getSimpleName());
        }
    }
}
