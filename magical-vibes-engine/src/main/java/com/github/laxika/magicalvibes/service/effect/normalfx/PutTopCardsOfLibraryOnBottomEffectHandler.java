package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfLibraryOnBottomEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Moves the top N cards of the controller's library to the bottom, in an order the controller
 * chooses (async {@code LibraryReorder} when 2+ cards move).
 */
@Component
@RequiredArgsConstructor
public class PutTopCardsOfLibraryOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTopCardsOfLibraryOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutTopCardsOfLibraryOnBottomEffect e = (PutTopCardsOfLibraryOnBottomEffect) effect;
        if (e.count() <= 0) {
            return;
        }

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) {
            return;
        }
        libraryRevealSupport.reorderRemainingToBottom(gameData, result.controllerId(), result.topCards());
    }
}
