package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsWithDifferentPowersToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves top-X reveals whose hand choices must have distinct powers. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsWithDifferentPowersToHandEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsWithDifferentPowersToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsWithDifferentPowersToHandEffect revealEffect =
                (RevealTopCardsWithDifferentPowersToHandEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, revealEffect.count(), AmountContext.forStackEntry(entry, source)));
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
        String playerName = result.playerName();
        List<Card> eligibleCards = topCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, revealEffect.predicate(), entry.getCard().getId(), gameData, controllerId))
                .toList();

        GameLog.Builder revealLog = GameLog.builder().text(playerName + " reveals ");
        appendCardList(revealLog, topCards);
        revealLog.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, revealLog.build());

        if (eligibleCards.isEmpty()) {
            bottomRandomly(gameData, controllerId, topCards, playerName);
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.LibraryRevealChoice
                .distinctPowersToHand(controllerId, topCards,
                        eligibleCards.stream().map(Card::getId).toList(),
                        "Choose any number of creature and/or Vehicle cards with different powers to put into your hand."));
    }

    private void bottomRandomly(GameData gameData, UUID controllerId, List<Card> cards, String playerName) {
        Collections.shuffle(cards);
        gameData.playerDecks.get(controllerId).addAll(cards);
        gameLogService.append(gameData, GameLog.text(playerName
                + " puts the revealed cards on the bottom of their library in a random order."));
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
