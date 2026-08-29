package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaCastTargetInstantOrSorceryFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaCastTargetInstantOrSorceryFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String cardName = sourceCard.getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue == null) {
            beginXPrompt(gameData, controllerId, cardName);
            return;
        }

        int chosenX = gameData.chosenXValue;
        gameData.chosenXValue = null;

        ManaPool pool = gameData.playerManaPools.get(controllerId);
        if (payableFromPool(pool) < chosenX) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " can't pay {" + chosenX + "} for " + cardName
                            + " (tap mana sources, then choose X again)."));
            beginXPrompt(gameData, controllerId, cardName);
            return;
        }

        new ManaCost("{X}").pay(pool, chosenX);
        gameLogService.append(gameData, GameLog.text(
                playerName + " pays {" + chosenX + "} for " + cardName + "."));
        beginReflexiveTrigger(gameData, entry, controllerId, chosenX);
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry,
                                       UUID controllerId, int chosenX) {
        Card sourceCard = entry.getCard();
        CardPredicate filter = manaValueInstantOrSorceryFilter(chosenX);
        CastTargetInstantOrSorceryFromGraveyardEffect castEffect =
                new CastTargetInstantOrSorceryFromGraveyardEffect(
                        GraveyardSearchScope.ALL_GRAVEYARDS, true, true, filter);

        List<Card> cardPool = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                cardPool.addAll(graveyard);
            }
        }

        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < cardPool.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(cardPool.get(i), filter, sourceCard.getId())) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    "'s ability has no instant or sorcery card with mana value " + chosenX
                            + " to target."));
            return;
        }

        if (matchingIndices.size() == 1) {
            Card targetCard = cardPool.get(matchingIndices.getFirst());
            addReflexiveTrigger(gameData, entry, castEffect, targetCard, chosenX);
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose an instant or sorcery card with mana value " + chosenX
                                + " from a graveyard to cast.")
                .cardPool(cardPool)
                .mayAbilityContext(sourceCard, controllerId, List.of(castEffect), entry.getSourcePermanentId())
                .build());
    }

    private void addReflexiveTrigger(GameData gameData, StackEntry entry,
                                     CastTargetInstantOrSorceryFromGraveyardEffect castEffect,
                                     Card targetCard, int chosenX) {
        Card sourceCard = entry.getCard();
        UUID controllerId = entry.getControllerId();
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(castEffect)),
                chosenX,
                targetCard.getId(),
                entry.getSourcePermanentId(),
                null,
                Zone.GRAVEYARD,
                null,
                null));
        gameLogService.append(gameData, GameLog.builder()
                .card(sourceCard)
                .text("'s ability targets ")
                .card(targetCard)
                .text(" in a graveyard.")
                .build());
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxX,
                        "Pay {X} for " + cardName
                                + "? Cast an instant or sorcery card with mana value X from a graveyard.",
                        cardName,
                        true));
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

    private static CardPredicate manaValueInstantOrSorceryFilter(int manaValue) {
        return new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                new CardMaxManaValuePredicate(manaValue),
                new CardMinManaValuePredicate(manaValue)));
    }
}
