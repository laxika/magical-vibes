package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsHandTopBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsHandTopBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsHandTopBottomEffect e = (LookAtTopCardsHandTopBottomEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        int count = topCards.size();

        if (count == 1) {
            // Only 1 card: it goes to hand
            gameData.addCardToHand(controllerId, topCards.getFirst());
            String logMsg = playerName + " looks at the top card of their library and puts it into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.HandTopBottomChoice(controllerId, topCards));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " looks at the top " + LibraryRevealSupport.pluralCards(count) + " of their library."));
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), count);
    
    }
}
