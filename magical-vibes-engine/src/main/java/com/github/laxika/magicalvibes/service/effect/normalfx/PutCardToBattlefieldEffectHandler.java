package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardToBattlefieldEffect) effect;

        // For "…and attach this Equipment to it" (Deathrender), the source Equipment is the trigger's card.
        java.util.UUID sourceEquipmentCardId = e.attachSourceEquipment() ? entry.getCard().getId() : null;
        playerInteractionSupport.applyPutCardToBattlefield(gameData, entry.getControllerId(), e, entry.getXValue(),
                sourceEquipmentCardId);

    }
}
