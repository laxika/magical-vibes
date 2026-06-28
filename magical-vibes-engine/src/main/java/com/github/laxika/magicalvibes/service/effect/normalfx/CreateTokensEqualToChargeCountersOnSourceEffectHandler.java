package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokensEqualToChargeCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensEqualToChargeCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokensEqualToChargeCountersOnSourceEffect) effect;
        
                int count = entry.getXValue();
                if (count <= 0) return;
                CreateTokenEffect tokenEffect = new CreateTokenEffect(
                        count, e.tokenName(), e.power(), e.toughness(),
                        e.color(), e.subtypes(), e.keywords(), e.additionalTypes()
                );
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    
    }
}
