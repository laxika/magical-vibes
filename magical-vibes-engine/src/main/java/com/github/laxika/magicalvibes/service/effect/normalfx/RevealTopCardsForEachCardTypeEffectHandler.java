package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForEachCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves one optional card selection for each represented card type. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsForEachCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsForEachCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsForEachCardTypeEffect e = (RevealTopCardsForEachCardTypeEffect) effect;
        List<CardType> cardTypes = new ArrayList<>(EnumSet.allOf(CardType.class));
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, Math.max(0, e.count()), false);
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        GameLog.Builder revealLog = GameLog.builder().text(playerName + " reveals ");
        appendCardList(revealLog, topCards);
        revealLog.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, revealLog.build());

        if (!beginNextTypePick(gameData, controllerId, topCards, cardTypes, 0)) {
            bottomRandomly(gameData, controllerId, topCards, playerName);
        }
    }

    private boolean beginNextTypePick(GameData gameData, UUID controllerId, List<Card> topCards,
            List<CardType> cardTypes, int typeIndex) {
        for (int i = typeIndex; i < cardTypes.size(); i++) {
            CardType type = cardTypes.get(i);
            List<Card> eligible = topCards.stream().filter(card -> card.hasType(type)).toList();
            if (eligible.isEmpty()) {
                continue;
            }

            String prompt = promptFor(type);
            LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(eligible))
                    .reveals(true)
                    .canFailToFind(true)
                    .destination(LibrarySearchDestination.HAND)
                    .sourceCards(new ArrayList<>(topCards))
                    .reorderRemainingToBottom(true)
                    .shuffleAfterSelection(false)
                    .followUp(LibrarySearchFollowUp.forCardTypeBoundedPick(
                            cardTypes.subList(i + 1, cardTypes.size())))
                    .prompt(prompt)
                    .build();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                    params, prompt, true));
            return true;
        }
        return false;
    }

    private void bottomRandomly(GameData gameData, UUID controllerId, List<Card> cards, String playerName) {
        java.util.Collections.shuffle(cards);
        gameData.playerDecks.get(controllerId).addAll(cards);
        gameLogService.append(gameData, GameLog.text(playerName
                + " puts the unchosen cards on the bottom of their library in a random order."));
    }

    private static String promptFor(CardType type) {
        return "You may reveal a " + type.getDisplayName().toLowerCase()
                + " card from among them and put it into your hand.";
    }

    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }
}
