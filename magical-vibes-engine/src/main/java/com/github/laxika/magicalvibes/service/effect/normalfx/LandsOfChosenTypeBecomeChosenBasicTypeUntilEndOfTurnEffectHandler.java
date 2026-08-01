package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfChosenTypeBecomeChosenBasicTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LandsOfChosenTypeBecomeChosenBasicTypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LandsOfChosenTypeBecomeChosenBasicTypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Prompt for a land type first; answering chains into the basic-land-type pick, which
        // then applies the type-replacing override to every matching land (Vision Charm).
        playerInputService.beginLandsOfTypeBecomeBasicTypeChoice(gameData, entry.getControllerId());
    }
}
