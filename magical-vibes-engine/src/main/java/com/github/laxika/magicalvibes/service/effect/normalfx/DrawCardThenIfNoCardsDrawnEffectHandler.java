package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardThenIfNoCardsDrawnEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a draw followed by a non-targeting rider only when no card was drawn. */
@Component
@RequiredArgsConstructor
public class DrawCardThenIfNoCardsDrawnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardThenIfNoCardsDrawnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardThenIfNoCardsDrawnEffect drawThen = (DrawCardThenIfNoCardsDrawnEffect) effect;
        int effectIndex = findEffectIndex(entry, effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Draw-if-no-cards effect is not on its stack entry");
        }

        if (drawThen.drawnCountBefore() == null) {
            entry.insertEffectsToResolve(effectIndex + 1, List.of(
                    drawThen.drawEffect(),
                    drawThen.continuation(entry.getDrawnCardIdsThisResolution().size())));
            return;
        }

        if (entry.getDrawnCardIdsThisResolution().size() == drawThen.drawnCountBefore()) {
            entry.insertEffectsToResolve(effectIndex + 1, List.of(drawThen.thenEffect()));
        }
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                return i;
            }
        }
        return -1;
    }
}
