package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GenesisWaveEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenesisWaveEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GenesisWaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int xValue = entry.getXValue();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (xValue <= 0 || deck.isEmpty()) {
            String logMsg = playerName + " casts Genesis Wave with X=" + xValue + " — no cards revealed.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int count = Math.min(xValue, deck.size());
        List<Card> revealedCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        List<Card> eligibleCards = new ArrayList<>();
        for (Card card : revealedCards) {
            boolean isPermanent = !card.hasType(CardType.INSTANT)
                    && !card.hasType(CardType.SORCERY);
            if (isPermanent && card.getManaValue() <= xValue) {
                eligibleCards.add(card);
            }
        }

        String logMsg = playerName + " reveals the top " + count + " cards of their library.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        if (eligibleCards.isEmpty()) {
            for (Card card : revealedCards) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card);
            }
            String graveyardLog = playerName + " finds no eligible permanent cards. All revealed cards are put into their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, graveyardLog);
            return;
        }

        List<UUID> cardIds = eligibleCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, revealedCards, cardIds, true, false, false, false, 0, null,
                eligibleCards.size(),
                "Choose any number of permanent cards with mana value " + xValue + " or less to put onto the battlefield."));

        log.info("Game {} - {} resolving Genesis Wave with X={}, {} revealed, {} eligible",
                gameData.id, playerName, xValue, count, eligibleCards.size());
    }
}
