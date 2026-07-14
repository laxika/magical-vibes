package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetSpellControllerDrawsCardEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellControllerDrawsCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellControllerId = findTargetSpellControllerId(gameData, entry.getTargetId());
        if (spellControllerId != null) {
            drawService.resolveDrawCard(gameData, spellControllerId);
        }
    }

    /** Controller of the spell on the stack whose card id matches {@code targetCardId}, or null. */
    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) return null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                return se.getControllerId();
            }
        }
        return null;
    }
}
