package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilChosenCreatureTypeCountToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Resolves Kindred Summons' chosen-type, dynamic-count library reveal. */
@Component
@RequiredArgsConstructor
public class RevealUntilChosenCreatureTypeCountToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final LegendRuleService legendRuleService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilChosenCreatureTypeCountToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        int requiredCount = countControlledCreaturesOfType(gameData, controllerId, chosenSubtype);
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (requiredCount <= 0 || library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " reveals no cards."));
            return;
        }

        CardAllOfPredicate matchingCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(chosenSubtype),
                        new CardKeywordPredicate(Keyword.CHANGELING)
                ))
        ));
        List<Card> revealedCards = new ArrayList<>();
        List<Card> matchingCards = new ArrayList<>();
        while (!library.isEmpty() && matchingCards.size() < requiredCount) {
            Card card = library.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, matchingCreature, entry.getCard().getId(), gameData, controllerId)) {
                matchingCards.add(card);
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " reveals " + revealedNames
                        + " from the top of their library."));

        List<Card> remainingCards = new ArrayList<>(revealedCards);
        remainingCards.removeAll(matchingCards);

        List<Card> placedCards = putMatchingCardsOntoBattlefield(
                gameData, controllerId, matchingCards, remainingCards);
        if (!remainingCards.isEmpty()) {
            library.addAll(remainingCards);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        }

        for (Card card : placedCards) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, card, null, false);
        }
        if (!placedCards.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private int countControlledCreaturesOfType(GameData gameData, UUID controllerId,
                                                CardSubtype subtype) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        PermanentHasSubtypePredicate predicate = new PermanentHasSubtypePredicate(subtype);
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && predicateEvaluationService.matchesPermanentPredicate(
                    permanent, predicate, filterContext)) {
                count++;
            }
        }
        return count;
    }

    private List<Card> putMatchingCardsOntoBattlefield(
            GameData gameData, UUID controllerId, List<Card> matchingCards,
            List<Card> remainingCards) {
        String playerName = gameData.playerIdToName.get(controllerId);
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Card> placedCards = new ArrayList<>();
        for (Card card : matchingCards) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
                remainingCards.add(card);
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " can't enter the battlefield from a library; it stays in the library."));
                continue;
            }

            Permanent permanent = new Permanent(card, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            placedCards.add(card);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, playerName));
        }
        return placedCards;
    }
}
