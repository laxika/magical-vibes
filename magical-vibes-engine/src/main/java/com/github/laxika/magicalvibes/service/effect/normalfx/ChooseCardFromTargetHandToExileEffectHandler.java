package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToExileEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChooseCardFromTargetHandToExileEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromTargetHandToExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseCardFromTargetHandToExileEffect) effect;

        UUID sourcePermanentId = e.returnOnSourceLeave() ? entry.getSourcePermanentId() : null;
        playerInteractionSupport.resolveHandRevealAndChoose(gameData, entry, e.count(), e.excludedTypes(), e.includedTypes(), false, true, sourcePermanentId);
    
    }
}
