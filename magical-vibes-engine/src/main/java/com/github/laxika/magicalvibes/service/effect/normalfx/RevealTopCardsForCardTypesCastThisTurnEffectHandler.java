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
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForCardTypesCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Hurkyl's dynamic one-card-per-card-type library selection. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsForCardTypesCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsForCardTypesCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsForCardTypesCastThisTurnEffect e =
                (RevealTopCardsForCardTypesCastThisTurnEffect) effect;
        List<CardType> cardTypes = cardTypesCastThisTurn(gameData, entry.getControllerId());
        if (cardTypes.isEmpty()) {
            return;
        }

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

    private List<CardType> cardTypesCastThisTurn(GameData gameData, UUID controllerId) {
        EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
        for (Card spell : gameData.getSpellsCastThisTurn(controllerId)) {
            if (spell.hasType(CardType.CREATURE)) {
                continue;
            }
            for (CardType type : CardType.values()) {
                if (spell.hasType(type)) {
                    types.add(type);
                }
            }
        }
        return new ArrayList<>(types);
    }

    private boolean beginNextTypePick(GameData gameData, UUID controllerId, List<Card> topCards,
            List<CardType> cardTypes, int typeIndex) {
        for (int i = typeIndex; i < cardTypes.size(); i++) {
            CardType type = cardTypes.get(i);
            List<Card> eligible = topCards.stream().filter(card -> card.hasType(type)).toList();
            if (eligible.isEmpty()) {
                continue;
            }

            LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(eligible))
                    .reveals(true)
                    .canFailToFind(true)
                    .destination(LibrarySearchDestination.HAND)
                    .sourceCards(new ArrayList<>(topCards))
                    .reorderRemainingToBottom(true)
                    .shuffleAfterSelection(false)
                    .followUp(LibrarySearchFollowUp.forCardTypeBoundedPick(
                            cardTypes.subList(i + 1, cardTypes.size())))
                    .prompt(promptFor(type))
                    .build();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                    params, promptFor(type), true));
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
