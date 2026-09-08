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
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final LegendRuleService legendRuleService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerRevealsTopCardsToBattlefieldRestToGraveyardEffect) effect;
        AmountContext baseContext = AmountContext.forStackEntry(entry, null);
        CardIsPermanentPredicate permanentPredicate = new CardIsPermanentPredicate();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        Map<UUID, List<Card>> permanentCardsByPlayer = new LinkedHashMap<>();
        Map<UUID, List<Permanent>> enteredByController = new LinkedHashMap<>();
        Map<UUID, Boolean> shuffledLibraries = new LinkedHashMap<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            int revealCount = Math.max(0, amountEvaluationService.evaluate(
                    gameData, e.amount(), baseContext.withControllerId(playerId)));
            int actualCount = Math.min(revealCount, library.size());
            if (actualCount == 0) {
                continue;
            }

            List<Card> revealedCards = LibraryRevealSupport.takeTopCards(library, actualCount);
            logReveal(gameData, entry, playerId, revealedCards);

            List<Card> permanentCards = new ArrayList<>();
            for (Card card : revealedCards) {
                if (predicateEvaluationService.matchesCardPredicate(
                        card, permanentPredicate, null, gameData, playerId)) {
                    permanentCards.add(card);
                } else {
                    graveyardService.addCardToGraveyard(gameData, playerId, card, Zone.LIBRARY);
                }
            }

            if (!permanentCards.isEmpty()) {
                permanentCardsByPlayer.put(playerId, permanentCards);
            }
        }

        for (Map.Entry<UUID, List<Card>> playerCards : permanentCardsByPlayer.entrySet()) {
            UUID playerId = playerCards.getKey();
            List<Card> library = gameData.playerDecks.get(playerId);
            for (Card card : playerCards.getValue()) {
                if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)) {
                    library.add(card);
                    shuffledLibraries.put(playerId, true);
                    continue;
                }

                Permanent permanent = new Permanent(card);
                initializeStartingCounters(permanent, card);
                UUID controllerId = battlefieldEntryService.resolveEnteringController(
                        gameData, playerId, permanent);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);

                if (gameQueryService.findPermanentById(gameData, permanent.getId()) != null) {
                    simultaneouslyEntered.add(permanent);
                    enteredByController.computeIfAbsent(controllerId, ignored -> new ArrayList<>()).add(permanent);
                }
            }
        }

        shuffledLibraries.forEach((playerId, ignored) ->
                Collections.shuffle(gameData.playerDecks.get(playerId)));

        enteredByController.forEach((controllerId, permanents) -> {
            for (Permanent permanent : permanents) {
                battlefieldEntryService.processCreatureETBEffects(
                        gameData, controllerId, permanent.getCard(), null, false);
            }
        });

        if (!gameData.interaction.isAwaitingInput()) {
            gameData.orderedPlayerIds.forEach(playerId -> legendRuleService.checkLegendRule(gameData, playerId));
        }
    }

    private void initializeStartingCounters(Permanent permanent, Card card) {
        if (card.hasType(CardType.PLANESWALKER)) {
            permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty() != null ? card.getLoyalty() : 0);
            permanent.setSummoningSick(false);
        } else if (card.hasType(CardType.BATTLE)) {
            permanent.setCounterCount(CounterType.DEFENSE, card.getDefense() != null ? card.getDefense() : 0);
            permanent.setSummoningSick(false);
        }
    }

    private void logReveal(GameData gameData, StackEntry entry, UUID playerId, List<Card> revealedCards) {
        GameLog.Builder builder = GameLog.builder()
                .text(gameData.playerIdToName.get(playerId) + " reveals ");
        for (int i = 0; i < revealedCards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(revealedCards.get(i));
        }
        builder.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, builder.build());
    }
}
