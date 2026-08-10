package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsPerHeadsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinsPerHeadsEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinsPerHeadsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FlipCoinsPerHeadsEffect) effect;

        int heads = 0;
        int physicalFlips = 0;
        for (int i = 0; i < e.coins(); i++) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, entry.getControllerId());
            physicalFlips += result.physicalFlips();
            if (result.heads()) {
                heads++;
            }
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        String replacementDetails = physicalFlips == e.coins()
                ? ""
                : " (" + physicalFlips + " physical coin flips; one result kept per coin)";
        gameLogService.append(gameData, GameLog.text(playerName + " flips " + e.coins() + " coins for "
                + entry.getCard().getName() + ": " + heads + " heads" + replacementDetails + "."));
        log.info("Game {} - {} flips {} coins for {}: {} heads", gameData.id, playerName, e.coins(),
                entry.getCard().getName(), heads);

        if (e.perHeads() == null) {
            return;
        }

        for (int i = 0; i < heads; i++) {
            dispatch(gameData, entry, e.perHeads());
        }
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        // SequenceEffect has no handler of its own — expand it here so a multi-step payload resolves
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
            log.warn("No handler for payload effect in FlipCoinsPerHeadsEffect: {}",
                    effect.getClass().getSimpleName());
        }
    }
}
