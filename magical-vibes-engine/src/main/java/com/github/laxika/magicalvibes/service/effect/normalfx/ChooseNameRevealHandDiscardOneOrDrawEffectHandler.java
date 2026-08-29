package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealHandDiscardOneOrDrawEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChooseNameRevealHandDiscardOneOrDrawEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNameRevealHandDiscardOneOrDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseNameRevealHandDiscardOneOrDrawEffect) effect;
        playerInputService.beginChooseNameRevealHandDiscardOneOrDrawChoice(
                gameData, entry.getControllerId(), entry.getTargetId(), e.excludedTypes());
    }
}
