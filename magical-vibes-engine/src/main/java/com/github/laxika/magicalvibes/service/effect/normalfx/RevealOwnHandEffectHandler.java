package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOwnHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RevealOwnHandEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealOwnHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null || !gameData.playerIds.contains(controllerId)) {
            return;
        }
        cardRevealService.revealHandToAllPlayers(gameData, controllerId);
    }
}
