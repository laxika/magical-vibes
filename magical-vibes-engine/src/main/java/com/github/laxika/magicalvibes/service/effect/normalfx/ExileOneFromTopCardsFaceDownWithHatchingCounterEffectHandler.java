package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithHatchingCounterEffect;
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
public class ExileOneFromTopCardsFaceDownWithHatchingCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final ExileService exileService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOneFromTopCardsFaceDownWithHatchingCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileOneFromTopCardsFaceDownWithHatchingCounterEffect exileEffect =
                (ExileOneFromTopCardsFaceDownWithHatchingCounterEffect) effect;
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, exileEffect.count(), true);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        if (topCards.size() == 1) {
            Card card = topCards.getFirst();
            exileService.exileCardFaceDown(gameData, controllerId, card, null);
            gameData.exiledCardsWithHatchingCounters.add(card.getId());
            gameLogService.append(gameData, GameLog.text(
                    result.playerName() + " exiles a card face down with a hatching counter."));
            return;
        }

        String prompt = "Exile one card face down with a hatching counter. Put the rest on the bottom of your library in any order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.EXILE_ONE_FACE_DOWN_WITH_HATCHING_COUNTER_REST_TO_BOTTOM)
                        .grantExilePlayPermission(false)
                        .allowAnyManaType(false)
                        .build(),
                prompt,
                false));
        log.info("Game {} - {} looks at the top {} cards of their library for a hatching-counter exile",
                gameData.id, result.playerName(), topCards.size());
    }
}
