package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TaintedPactEffect;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaintedPactEffectHandler implements NormalEffectHandlerBean {

    private final TaintedPactSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TaintedPactEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        support.exileTopCardAndOfferToHand(gameData, entry.getCard(), entry.getControllerId(), List.of());
    }
}
