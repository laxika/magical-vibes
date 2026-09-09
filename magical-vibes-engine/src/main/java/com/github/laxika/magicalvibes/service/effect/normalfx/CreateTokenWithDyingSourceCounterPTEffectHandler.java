package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCounterPTEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenWithDyingSourceCounterPTEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithDyingSourceCounterPTEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int counters = Math.max(0, entry.getEventValue());
        CreateTokenEffect template = ((CreateTokenWithDyingSourceCounterPTEffect) effect).tokenTemplate();
        CreateTokenEffect resolved = template.withPowerToughness(counters, counters);
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), resolved, entry.getCard().getSetCode()));
    }
}
