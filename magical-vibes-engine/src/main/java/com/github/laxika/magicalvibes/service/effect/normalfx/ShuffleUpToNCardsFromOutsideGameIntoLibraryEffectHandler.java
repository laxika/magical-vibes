package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleUpToNCardsFromOutsideGameIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves Research's choice of cards owned from outside the game. */
@Component
@RequiredArgsConstructor
public class ShuffleUpToNCardsFromOutsideGameIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleUpToNCardsFromOutsideGameIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ShuffleUpToNCardsFromOutsideGameIntoLibraryEffect) effect;
        List<Card> sideboard = gameData.playerSideboards.getOrDefault(entry.getControllerId(), List.of());
        if (e.maxCount() <= 0 || sideboard.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ShuffleCardsFromOutsideGameChoice(
                        entry.getControllerId(), new ArrayList<>(sideboard), e.maxCount()));
    }
}
