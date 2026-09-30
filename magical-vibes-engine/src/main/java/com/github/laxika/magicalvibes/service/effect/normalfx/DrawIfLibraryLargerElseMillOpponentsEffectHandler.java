package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawIfLibraryLargerElseMillOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DrawIfLibraryLargerElseMillOpponentsEffectHandler implements NormalEffectHandlerBean {

    private final DrawCardEffectHandler drawCardEffectHandler;
    private final MillEffectHandler millEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawIfLibraryLargerElseMillOpponentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        if (targetId == null || !gameData.playerIds.contains(targetId)) {
            return;
        }

        if (gameData.playerDecks.get(controllerId).size() > gameData.playerDecks.get(targetId).size()) {
            drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(1));
        } else {
            millEffectHandler.resolve(gameData, entry, new MillEffect(5, MillRecipient.EACH_OPPONENT));
        }
    }
}
