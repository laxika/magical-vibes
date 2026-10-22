package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileOneFromTopCardsFaceDownWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final ExileService exileService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOneFromTopCardsFaceDownWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileOneFromTopCardsFaceDownWithSourceEffect exileEffect =
                (ExileOneFromTopCardsFaceDownWithSourceEffect) effect;
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, exileEffect.count(), true);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (topCards.size() == 1) {
            Card exiled = topCards.getFirst();
            exileService.exileCardFaceDown(gameData, controllerId, exiled, sourcePermanentId);
            if (exileEffect.castPermissionFilter() != null) {
                gameData.exilePlayPermissions.put(exiled.getId(), controllerId);
                gameData.exilePlayPermissionSpellFilters.put(exiled.getId(), exileEffect.castPermissionFilter());
            }
            gameLogService.append(gameData, GameLog.text(
                    result.playerName() + " exiles a card face down with " + entry.getCard().getName() + "."));
            return;
        }

        String prompt = exileEffect.randomOrder()
                ? "Exile one card face down. Put the rest on the bottom of your library in a random order."
                : "Exile one card face down. Put the rest on the bottom of your library in any order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(exileEffect.randomOrder()
                                ? LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM
                                : LibrarySearchDestination.EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM)
                        .sourcePermanentId(sourcePermanentId)
                        .grantExilePlayPermission(exileEffect.castPermissionFilter() != null)
                        .allowAnyManaType(false)
                        .exilePlayPermissionFilter(exileEffect.castPermissionFilter())
                        .build(),
                prompt,
                false));
        log.info("Game {} - {} looks at the top {} cards of their library for a face-down exile",
                gameData.id, result.playerName(), topCards.size());
    }
}
