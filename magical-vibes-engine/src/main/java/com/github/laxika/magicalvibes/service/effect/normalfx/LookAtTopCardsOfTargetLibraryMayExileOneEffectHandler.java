package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryMayExileOneEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsOfTargetLibraryMayExileOneEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsOfTargetLibraryMayExileOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsOfTargetLibraryMayExileOneEffect e = (LookAtTopCardsOfTargetLibraryMayExileOneEffect) effect;

        UUID controllerId = entry.getControllerId();
        // No target (e.g. Puresight Merrow's untap ability) means the controller looks at their own library.
        UUID targetPlayerId = entry.getTargetId() != null ? entry.getTargetId() : controllerId;
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        int actual = Math.min(e.count(), deck.size());
        if (actual == 0) {
            String logMsg = entry.getCard().getName() + ": " + targetName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);

        String logMsg = controllerName + " looks at the top " + LibraryRevealSupport.pluralCards(actual) + " of " + targetName + "'s library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));

        List<Card> sourceCards = new ArrayList<>(topCards);

        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                .canFailToFind(true)
                .targetPlayerId(targetPlayerId)
                .sourceCards(sourceCards)
                .reorderRemainingToTop(true)
                .shuffleAfterSelection(false)
                .prompt("You may exile one of these cards. The rest will be put on top of the library.")
                .destination(LibrarySearchDestination.EXILE)
                .build(),
                "You may exile one of these cards. The rest will be put on top of the library.",
                true));

        log.info("Game {} - {} looks at top {} of {}'s library ({})", gameData.id, controllerName, actual, targetName, entry.getCard().getName());
    
    }
}
