package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreaturesWithTotalManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaReanimateCreaturesWithTotalManaValueXEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaReanimateCreaturesWithTotalManaValueXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String cardName = sourceCard.getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + " chooses X=0 for " + cardName + "'s ability."));
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

            new ManaCost("{X}").pay(pool, chosenValue);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            beginReflexiveTrigger(gameData, entry, controllerId, chosenValue);
            return;
        }

        if (maxPotentialX(gameData, controllerId) <= 0) {
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " has no mana to pay for ", sourceCard, "'s ability."));
            return;
        }
        beginXPrompt(gameData, controllerId, cardName);
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry,
                                       UUID controllerId, int x) {
        UUID damagedPlayerId = entry.getTargetId();
        Card sourceCard = entry.getCard();
        CardPredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
        ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect =
                ReturnTargetCardsFromGraveyardToBattlefieldEffect.withinTotalManaValueFromAllGraveyards(
                        creatureFilter, x);

        List<Card> candidates = new ArrayList<>();
        if (damagedPlayerId != null && gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            for (Card card : gameData.playerGraveyards.getOrDefault(damagedPlayerId, List.of())) {
                if (card.getManaValue() <= x
                        && !gameQueryService.isLandCardTargetRestricted(gameData, card, controllerId)
                        && predicateEvaluationService.matchesCardPredicate(
                        card, creatureFilter, sourceCard.getId())) {
                    candidates.add(card);
                }
            }
        }

        if (candidates.isEmpty()) {
            putReflexiveTriggerOnStack(gameData, entry, controllerId, x, returnEffect, List.of());
            return;
        }

        gameData.graveyardTargetOperation.card = sourceCard;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(List.of(returnEffect));
        gameData.graveyardTargetOperation.xValue = x;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.singleGraveyard = true;
        gameData.graveyardTargetOperation.sourcePermanentId = entry.getSourcePermanentId();

        String prompt = "Choose any number of target creature cards from that player's graveyard "
                + "with total mana value " + x + " or less.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                controllerId, candidates, candidates.size(), prompt, 0, x));
    }

    private void putReflexiveTriggerOnStack(GameData gameData, StackEntry entry, UUID controllerId,
                                            int x, ReturnTargetCardsFromGraveyardToBattlefieldEffect effect,
                                            List<UUID> targetCardIds) {
        Card sourceCard = entry.getCard();
        StackEntry triggeredEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                x,
                null,
                entry.getSourcePermanentId(),
                Map.of(),
                null,
                new ArrayList<>(targetCardIds),
                List.of());
        gameData.stack.add(triggeredEntry);
        gameLogService.append(gameData, GameLog.text(
                sourceCard.getName() + "'s ability triggers targeting no cards."));
        log.info("Game {} - {}'s reflexive reanimate trigger has no targets", gameData.id, sourceCard.getName());
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX,
                        "Pay {X} for " + cardName + "? Return creature cards from that player's graveyard.",
                        cardName, true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources;
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless()
                + pool.getMyrOnlyColorless() + pool.getXCostOnlyColorless();
    }
}
