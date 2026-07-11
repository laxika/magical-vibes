package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutOneOnTopRestOnBottomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Look at the top N cards of your library, put one of those cards on top of your library and the
 * rest on the bottom of your library in any order (Cream of the Crop). Reuses the single-pick
 * {@link PendingInteraction.LibrarySearch} flow with {@code reorderRemainingToBottom} and the
 * {@link LibrarySearchDestination#TOP_OF_LIBRARY} chosen-destination (chosen card back on top,
 * no shuffle), exactly like {@link ImprintFromTopCardsEffectHandler} but keeping the pick on top.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsPutOneOnTopRestOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsPutOneOnTopRestOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsPutOneOnTopRestOnBottomEffect e = (LookAtTopCardsPutOneOnTopRestOnBottomEffect) effect;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (topCards.size() == 1) {
            // Only one card looked at — it goes back on top, nothing to put on the bottom.
            gameData.playerDecks.get(controllerId).addFirst(topCards.getFirst());
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts a card on top of their library.");
            return;
        }

        List<Card> sourceCards = new ArrayList<>(topCards);
        String prompt = "Put one card on top of your library. The rest go to the bottom of your library.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .sourceCards(sourceCards)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                        .build(),
                prompt,
                false));
    }
}
