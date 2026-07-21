package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealHandDamagePerCopyAndExileEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChooseNameRevealHandDamagePerCopyAndExileEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseNameRevealHandDamagePerCopyAndExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseNameRevealHandDamagePerCopyAndExileEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        playerInputService.beginRevealHandDamageAndExileCardNameChoice(
                gameData, controllerId, targetPlayerId, e.excludedTypes(), e.damagePerCard(), entry.getCard());
    }
}
