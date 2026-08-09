package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Opens a modal choice for the player identified by the stack entry's target. */
@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesOneEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID chooserId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        TargetPlayerChoosesOneEffect targetChoice = (TargetPlayerChoosesOneEffect) effect;
        playerInputService.beginChooseModeChoice(gameData, chooserId, entry.getCard(),
                new ChooseOneEffect(targetChoice.options()));
    }
}
