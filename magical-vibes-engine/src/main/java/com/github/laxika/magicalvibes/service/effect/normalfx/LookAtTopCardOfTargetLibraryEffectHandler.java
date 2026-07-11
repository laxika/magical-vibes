package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardOfTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardOfTargetLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + ": " + targetName + "'s library is empty.");
            return;
        }

        // Public log records only that a look happened, never the card's identity (private look).
        gameBroadcastService.logAndBroadcast(gameData,
                controllerName + " looks at the top card of " + targetName + "'s library.");

        // Surface the top card privately to the controller. It always stays on top: declining leaves
        // it in place (reorderRemainingToTop), "selecting" it puts it back on top (TOP_OF_LIBRARY).
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, 1);
        String prompt = "The top card of " + targetName + "'s library. It will remain on top.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, topCards)
                        .canFailToFind(true)
                        .targetPlayerId(targetPlayerId)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToTop(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.TOP_OF_LIBRARY)
                        .build(),
                prompt,
                true));

        log.info("Game {} - {} looks at top card of {}'s library ({})",
                gameData.id, controllerName, targetName, entry.getCard().getName());
    }
}
