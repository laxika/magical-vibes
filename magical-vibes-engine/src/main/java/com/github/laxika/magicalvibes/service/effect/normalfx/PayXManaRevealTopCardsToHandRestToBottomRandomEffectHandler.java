package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaRevealTopCardsToHandRestToBottomRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayXManaRevealTopCardsToHandRestToBottomRandomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaRevealTopCardsToHandRestToBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaRevealTopCardsToHandRestToBottomRandomEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " declines to pay for " + cardName + "'s ability."));
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }

            new ManaCost("{0}").pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + chosenValue + "} for " + cardName + "."));

            LibraryRevealSupport.TopCardsResult result =
                    libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, chosenValue);
            if (result == null) {
                return;
            }

            List<Card> topCards = result.topCards();
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals " + cardNames(topCards) + " from the top of their library with "
                            + cardName + "."));

            List<Card> matchingCards = new ArrayList<>();
            for (Card card : topCards) {
                if (predicateEvaluationService.matchesCardPredicate(
                        card, e.filter(), entry.getCard().getId(), gameData, controllerId)) {
                    matchingCards.add(card);
                    gameData.addCardToHand(controllerId, card);
                }
            }
            if (!matchingCards.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " puts " + cardNames(matchingCards) + " into their hand."));
            }

            List<Card> remainingCards = new ArrayList<>(topCards);
            remainingCards.removeAll(matchingCards);
            if (!remainingCards.isEmpty()) {
                Collections.shuffle(remainingCards);
                gameData.playerDecks.get(controllerId).addAll(remainingCards);
            }
            return;
        }

        if (maxPotentialX(gameData, controllerId) <= 0) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " has no mana to pay for " + cardName + "'s ability."));
            return;
        }
        beginXPrompt(gameData, controllerId, cardName);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxX,
                        "You may pay {X} for " + cardName + ". Choose X (0 = don't pay).",
                        cardName,
                        true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return Math.max(0, payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources);
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
    }

    private static String cardNames(List<Card> cards) {
        return cards.stream().map(Card::getName).reduce((first, second) -> first + ", " + second).orElse("");
    }
}
