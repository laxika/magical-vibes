package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MorphicTideBottomCardsEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MorphicTideBottomCardsEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MorphicTideBottomCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MorphicTideBottomCardsEffect bottom = (MorphicTideBottomCardsEffect) effect;
        libraryRevealSupport.reorderRemainingToBottom(gameData, bottom.playerId(), bottom.cards());
    }
}
