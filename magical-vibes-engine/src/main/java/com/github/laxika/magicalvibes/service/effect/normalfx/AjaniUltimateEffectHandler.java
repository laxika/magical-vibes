package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AjaniUltimateEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AjaniUltimateEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AjaniUltimateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int lifeTotal = gameData.getLife(controllerId);

        int count = Math.min(lifeTotal, deck.size());
        if (count <= 0) {
            String logMsg = playerName + " looks at no cards (library is empty or life total is 0). Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        // Take top X cards from library
        List<Card> revealedCards = LibraryRevealSupport.takeTopCards(deck, count);

        // Filter to nonland permanent cards with MV <= 3
        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (card.getType() != null
                    && !card.hasType(CardType.LAND)
                    && !card.hasType(CardType.INSTANT)
                    && !card.hasType(CardType.SORCERY)
                    && card.getManaValue() <= 3) {
                eligibleCards.add(card);
            }
        }

        String logMsg = playerName + " looks at the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        if (eligibleCards.isEmpty()) {
            // No eligible cards — put all back and shuffle
            deck.addAll(revealedCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String shuffleLog = playerName + " finds no eligible cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            return;
        }

        // Set up player choice for selecting cards to put onto battlefield
        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, revealedCards, cardIds, false, false, false, false, 0, null,
                eligibleCards.size(),
                "Choose any number of nonland permanent cards with mana value 3 or less to put onto the battlefield."));

        log.info("Game {} - {} resolving Ajani ultimate with {} revealed, {} eligible", gameData.id, playerName, count, eligibleCards.size());
    
    }
}
