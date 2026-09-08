package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMatchingCardsFromGraveyardAndLibraryMayCastThisTurnEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileMatchingCardsFromGraveyardAndLibraryMayCastThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileMatchingCardsFromGraveyardAndLibrarySupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileMatchingCardsFromGraveyardAndLibraryMayCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileMatchingCardsFromGraveyardAndLibraryMayCastThisTurnEffect) effect;
        support.begin(gameData, entry.getControllerId(), exileEffect.filter());
    }
}
