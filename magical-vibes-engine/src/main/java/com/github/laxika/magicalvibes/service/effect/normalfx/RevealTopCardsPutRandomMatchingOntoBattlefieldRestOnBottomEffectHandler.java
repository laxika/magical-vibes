package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/** Resolves a fixed-count library reveal with one random matching battlefield placement. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect) effect;
        UUID controllerId = entry.getControllerId();
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, Math.max(0, typedEffect.count()));
        if (result == null) {
            return;
        }

        List<Card> revealedCards = result.topCards();
        gameLogService.append(gameData, GameLog.text(
                result.playerName() + " reveals "
                        + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + " from the top of their library."));

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> matchingCards = revealedCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, typedEffect.predicate(), sourceCardId, gameData, controllerId))
                .toList();

        Card chosenCard = matchingCards.isEmpty()
                ? null
                : matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        List<Card> remainingCards = new ArrayList<>(revealedCards);
        if (chosenCard != null && !gameQueryService.isCardBlockedFromEnteringFromZone(
                gameData, chosenCard, Zone.LIBRARY)) {
            remainingCards.remove(chosenCard);
            Permanent permanent = new Permanent(chosenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            if (chosenCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, controllerId, chosenCard, null, false);
            }
            gameLogService.append(gameData,
                    GameLog.entersBattlefieldUnder(chosenCard, result.playerName()));
        }

        Collections.shuffle(remainingCards);
        gameData.playerDecks.get(controllerId).addAll(remainingCards);
    }
}
