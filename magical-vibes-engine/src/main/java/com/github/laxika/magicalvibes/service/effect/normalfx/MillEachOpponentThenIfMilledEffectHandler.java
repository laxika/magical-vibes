package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachOpponentThenIfMilledEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves "each opponent mills N cards; if cards were milled this way, [follow-up]". */
@Slf4j
@Component
@RequiredArgsConstructor
public class MillEachOpponentThenIfMilledEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachOpponentThenIfMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillEachOpponentThenIfMilledEffect millEffect = (MillEachOpponentThenIfMilledEffect) effect;
        List<Card> milled = new ArrayList<>();
        for (var playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(entry.getControllerId())) {
                milled.addAll(graveyardService.resolveMillPlayer(gameData, playerId, millEffect.count()));
            }
        }

        entry.setEventValue(milled.size());
        if (milled.isEmpty()) {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(millEffect.thenEffect());
        if (handler == null) {
            log.warn("No handler for follow-up effect in MillEachOpponentThenIfMilledEffect: {}",
                    millEffect.thenEffect().getClass().getSimpleName());
            return;
        }
        handler.resolve(gameData, entry, millEffect.thenEffect());
    }
}
