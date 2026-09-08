package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLooksAtHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetSpellControllerLooksAtHandEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellControllerLooksAtHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellControllerId = findTargetSpellControllerId(gameData, entry.getTargetId());
        if (spellControllerId != null) {
            cardRevealService.lookAtHand(gameData, entry.getControllerId(), spellControllerId);
        }
    }

    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) return null;
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)) {
                return stackEntry.getControllerId();
            }
        }
        return null;
    }
}
