package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetPlayerLibraryUntilPredicateCountToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Resolves a multi-card creature reveal with simultaneous battlefield entry. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTargetPlayerLibraryUntilPredicateCountToBattlefieldRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTargetPlayerLibraryUntilPredicateCountToBattlefieldRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealTargetPlayerLibraryUntilPredicateCountToBattlefieldRestToGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int requiredCount = amountEvaluationService.evaluate(
                gameData, typedEffect.requiredCount(), AmountContext.forStackEntry(entry, null));
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (requiredCount <= 0 || deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    targetName + " reveals no cards from their library."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        List<Card> matchingCards = new ArrayList<>();
        while (!deck.isEmpty() && matchingCards.size() < requiredCount) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                matchingCards.add(card);
            }
        }

        gameLogService.append(gameData, GameLog.text(
                targetName + " reveals "
                        + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + " from the top of their library."));

        Set<CardType> enterTappedTypesSnapshot = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Permanent> enteredPermanents = new ArrayList<>();
        List<Card> enteredCards = new ArrayList<>();
        List<Card> blockedCards = new ArrayList<>();
        for (Card matchingCard : matchingCards) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, matchingCard, Zone.LIBRARY)) {
                blockedCards.add(matchingCard);
                gameLogService.append(gameData, GameLog.cardThen(matchingCard,
                        " can't enter the battlefield from a library; it stays in the library."));
                continue;
            }

            Permanent permanent = new Permanent(matchingCard);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypesSnapshot, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            enteredCards.add(matchingCard);
            enteredPermanents.add(permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(matchingCard, controllerName));
        }

        for (int i = 0; i < enteredCards.size(); i++) {
            Card creatureCard = enteredCards.get(i);
            Permanent permanent = enteredPermanents.get(i);
            battlefieldEntryService.processCreatureETBEffects(
                    gameData, controllerId, creatureCard, null, false);
            if (creatureCard.hasType(CardType.PLANESWALKER) && creatureCard.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, creatureCard.getLoyalty());
                permanent.setSummoningSick(false);
            }
        }

        List<Card> rest = new ArrayList<>(revealedCards);
        rest.removeAll(matchingCards);
        rest.addAll(blockedCards);
        for (Card card : rest) {
            if (!blockedCards.contains(card)) {
                graveyardService.addCardToGraveyard(gameData, targetPlayerId, card, Zone.LIBRARY);
            } else {
                deck.add(card);
            }
        }

        log.info("Game {} - {} revealed {} cards from {}'s library and put {} matching card(s) onto the battlefield",
                gameData.id, controllerName, revealedCards.size(), targetName, enteredCards.size());
    }
}
