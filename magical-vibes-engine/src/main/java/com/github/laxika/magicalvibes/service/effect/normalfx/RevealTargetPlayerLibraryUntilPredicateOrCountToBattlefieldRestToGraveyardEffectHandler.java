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
import com.github.laxika.magicalvibes.model.effect.RevealTargetPlayerLibraryUntilPredicateOrCountToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTargetPlayerLibraryUntilPredicateOrCountToBattlefieldRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTargetPlayerLibraryUntilPredicateOrCountToBattlefieldRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealTargetPlayerLibraryUntilPredicateOrCountToBattlefieldRestToGraveyardEffect) effect;
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
        Card foundCard = null;
        while (!deck.isEmpty() && revealedCards.size() < requiredCount) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                foundCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                targetName + " reveals " + revealedNames + " from the top of their library."));

        if (foundCard != null) {
            revealedCards.remove(foundCard);
            Permanent permanent = new Permanent(foundCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCard, controllerName));

            if (foundCard.hasType(CardType.PLANESWALKER) && foundCard.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, foundCard.getLoyalty());
                permanent.setSummoningSick(false);
            }
            if (foundCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, controllerId, foundCard, null, false);
            }

            sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());
        } else {
            gameLogService.append(gameData, GameLog.text(
                    targetName + " reveals " + revealedCards.size() + " cards without finding a matching card."));
        }

        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, targetPlayerId, card, Zone.LIBRARY);
        }

        log.info("Game {} - {} reveals {} cards from {}'s library, matching card found={}",
                gameData.id, controllerName, revealedCards.size() + (foundCard != null ? 1 : 0),
                targetName, foundCard != null ? foundCard.getName() : "none");
    }
}
