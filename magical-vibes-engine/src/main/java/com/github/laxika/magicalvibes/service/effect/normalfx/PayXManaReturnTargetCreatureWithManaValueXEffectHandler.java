package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReturnTargetCreatureWithManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaReturnTargetCreatureWithManaValueXEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ReturnCardFromGraveyardEffectHandler returnCardFromGraveyardEffectHandler;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaReturnTargetCreatureWithManaValueXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaReturnTargetCreatureWithManaValueXEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String cardName = sourceCard.getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (entry.getTargetId() != null && gameData.chosenXValue != null) {
            int chosenX = gameData.chosenXValue;
            gameData.chosenXValue = null;
            returnTarget(gameData, entry, e.filter(), chosenX);
            return;
        }

        if (gameData.chosenXValue == null) {
            if (maxPotentialX(gameData, controllerId) <= 0) {
                return;
            }
            beginXPrompt(gameData, controllerId, cardName);
            return;
        }

        int chosenX = gameData.chosenXValue;
        if (chosenX == 0) {
            gameData.chosenXValue = null;
            gameLogService.append(gameData, GameLog.text(playerName + " chooses X=0 for " + cardName + "'s ability."));
            return;
        }

        ManaPool pool = gameData.playerManaPools.get(controllerId);
        if (payableFromPool(pool) < chosenX) {
            beginXPrompt(gameData, controllerId, cardName);
            return;
        }

        new ManaCost("{X}").pay(pool, chosenX);
        gameLogService.append(gameData, GameLog.text(playerName + " pays {" + chosenX + "} for " + cardName + "."));

        CardPredicate filter = manaValueCreatureFilter(e.filter(), chosenX);
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<Integer> matchingIndices = new ArrayList<>();
        if (graveyard != null) {
            for (int i = 0; i < graveyard.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), filter, sourceCard.getId())) {
                    matchingIndices.add(i);
                }
            }
        }

        if (matchingIndices.isEmpty()) {
            gameData.chosenXValue = null;
            gameLogService.append(gameData, GameLog.text(cardName + " has no legal graveyard target for X=" + chosenX + "."));
            return;
        }

        if (matchingIndices.size() == 1) {
            Card target = graveyard.get(matchingIndices.getFirst());
            gameData.chosenXValue = null;
            entry.setTargetId(target.getId());
            triggerCollectionService.checkTargetChoiceTriggers(gameData, entry);
            returnTarget(gameData, entry, e.filter(), chosenX);
            return;
        }

        gameData.resolvedMayTargetingEntry = entry;
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose a matching creature card with mana value " + chosenX
                                + " to return to the battlefield.")
                .build());
    }

    private void returnTarget(GameData gameData, StackEntry entry, CardPredicate filter, int chosenX) {
        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(manaValueCreatureFilter(filter, chosenX))
                .targetGraveyard(true)
                .build();
        returnCardFromGraveyardEffectHandler.resolve(gameData, entry, returnEffect);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX,
                        "Pay {X} for " + cardName + "? Return a matching creature card with mana value X.",
                        cardName, true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return Math.max(0, payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources);
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless()
                + pool.getMyrOnlyColorless() + pool.getXCostOnlyColorless();
    }

    private static CardPredicate manaValueCreatureFilter(CardPredicate filter, int x) {
        List<CardPredicate> predicates = new ArrayList<>(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(x),
                new CardMinManaValuePredicate(x)));
        if (filter != null) {
            predicates.add(filter);
        }
        return new CardAllOfPredicate(predicates);
    }
}
