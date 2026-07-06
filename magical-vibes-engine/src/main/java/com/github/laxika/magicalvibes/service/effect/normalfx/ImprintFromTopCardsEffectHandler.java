package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImprintFromTopCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ImprintFromTopCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ImprintFromTopCardsEffect e = (ImprintFromTopCardsEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();

        if (topCards.size() == 1) {
            // Only one card — must exile it, nothing to reorder
            exileService.exileCard(gameData, controllerId, topCards.getFirst());
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId != null) {
                gameQueryService.setImprintedCardOnPermanent(gameData, sourcePermanentId, topCards.getFirst());
            }
            String exileLog = playerName + " exiles a card face down with " + entry.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
            return;
        }

        List<Card> sourceCards = new ArrayList<>(topCards);

        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                .sourceCards(sourceCards)
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt("Exile one card face down (imprint). The rest go to the bottom of your library.")
                .destination(LibrarySearchDestination.EXILE_IMPRINT)
                .followUp(com.github.laxika.magicalvibes.model.LibrarySearchFollowUp.imprint(entry.getSourcePermanentId()))
                .build(),
                "Exile one card face down (imprint). The rest go to the bottom of your library.",
                false));
    
    }
}
