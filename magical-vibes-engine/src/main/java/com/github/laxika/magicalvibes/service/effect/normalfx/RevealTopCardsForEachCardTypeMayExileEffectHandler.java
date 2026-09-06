package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForEachCardTypeMayExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Portent of Calamity's one-card-per-card-type library reveal. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsForEachCardTypeMayExileEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsForEachCardTypeMayExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsForEachCardTypeMayExileEffect e =
                (RevealTopCardsForEachCardTypeMayExileEffect) effect;
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.count(), AmountContext.forStackEntry(entry, null)));
        if (count == 0) {
            return;
        }

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, count);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        gameLogService.append(gameData, revealLog(result.playerName(), topCards, entry));
        gameData.queueInteraction(new PendingInteraction.PortentOfCalamityState(
                controllerId, topCards.stream().map(Card::getId).toList()));

        if (!beginNextTypePick(gameData, controllerId, topCards, CardType.values(), 0)) {
            putRemainingIntoGraveyard(gameData, controllerId, topCards);
            gameData.pollPendingInteraction(PendingInteraction.PortentOfCalamityState.class);
        }
    }

    private boolean beginNextTypePick(GameData gameData, UUID controllerId, List<Card> topCards,
                                      CardType[] cardTypes, int typeIndex) {
        for (int i = typeIndex; i < cardTypes.length; i++) {
            CardType type = cardTypes[i];
            List<Card> eligible = topCards.stream().filter(card -> card.hasType(type)).toList();
            if (eligible.isEmpty()) {
                continue;
            }

            LibrarySearchFollowUp followUp = LibrarySearchFollowUp.forCardTypeBoundedPick(
                    Arrays.asList(cardTypes).subList(i + 1, cardTypes.length),
                    LibrarySearchDestination.EXILE, true);
            String prompt = "You may reveal a " + type.getDisplayName().toLowerCase()
                    + " card from among them and exile it.";
            LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(eligible))
                    .reveals(true)
                    .canFailToFind(true)
                    .destination(LibrarySearchDestination.EXILE)
                    .sourceCards(new ArrayList<>(topCards))
                    .restToGraveyard(true)
                    .shuffleAfterSelection(false)
                    .followUp(followUp)
                    .prompt(prompt)
                    .build();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                    params, prompt, true));
            return true;
        }
        return false;
    }

    private void putRemainingIntoGraveyard(GameData gameData, UUID ownerId, List<Card> cards) {
        for (Card card : cards) {
            graveyardService.addCardToGraveyard(gameData, ownerId, card, Zone.LIBRARY);
        }
        if (!cards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(ownerId) + " puts the revealed cards into their graveyard."));
        }
    }

    private static GameLogEntry revealLog(String playerName, List<Card> cards, StackEntry entry) {
        GameLog.Builder builder = GameLog.builder().text(playerName + " reveals ");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder.text(" from the top of their library with ").card(entry.getCard()).text(".").build();
    }
}
