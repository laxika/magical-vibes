package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestOneOfTopTwoEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves looking at two library cards and choosing one to manifest. */
@Component
@RequiredArgsConstructor
public class ManifestOneOfTopTwoEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestOneOfTopTwoEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, 2, true);
        if (result == null) {
            return;
        }

        List<Card> topCards = result.topCards();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.LibraryRevealChoice.manifestOneOfTopTwo(
                result.controllerId(), topCards,
                "Choose one of these cards to manifest. Put the other on the top or bottom of your library."));
    }
}
