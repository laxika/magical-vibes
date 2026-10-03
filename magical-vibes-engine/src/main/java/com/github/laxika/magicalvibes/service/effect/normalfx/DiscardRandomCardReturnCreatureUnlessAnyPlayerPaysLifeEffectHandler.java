package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the discard portion of Aether Rift's upkeep ability. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var aetherRift = (DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " has no cards to discard."));
            return;
        }

        Card discarded = hand.get(ThreadLocalRandom.current().nextInt(hand.size()));
        gameData.discardCausedByOpponent = false;
        if (!discarded.isToken()
                && graveyardService.findDiscardToLibraryReplacementSource(gameData, controllerId) != null) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(entry.getCard(), controllerId,
                    List.of(new DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect(
                            aetherRift.lifeCost(), discarded.getId(), controllerId, List.of(), true)),
                    "Put " + discarded.getName() + " on top of your library instead of into your graveyard?",
                    null, null, entry.getSourcePermanentId()));
            return;
        }
        discardAndContinue(gameData, entry.getCard(), entry.getSourcePermanentId(), controllerId,
                discarded.getId(), aetherRift.lifeCost(), false);
    }

    public void discardAndContinue(GameData gameData, Card sourceCard, UUID sourcePermanentId,
                                   UUID controllerId, UUID discardedCardId, int lifeCost,
                                   boolean applyLibraryReplacement) {
        List<Card> hand = gameData.playerHands.get(controllerId);
        Card discarded = hand.stream().filter(card -> card.getId().equals(discardedCardId)).findFirst().orElse(null);
        if (discarded == null) return;
        hand.remove(discarded);
        gameData.discardCausedByOpponent = false;
        boolean enteredGraveyard = graveyardService.discardCard(gameData, controllerId, discarded,
                applyLibraryReplacement);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " discards ", discarded, " at random."));
        log.info("Game {} - {} discards {} at random ({})", gameData.id,
                gameData.playerIdToName.get(controllerId), discarded.getName(), sourceCard.getName());
        triggerCollectionService.checkDiscardTriggers(gameData, controllerId, discarded);

        if (!enteredGraveyard || !gameQueryService.cardHasType(discarded, CardType.CREATURE, gameData, controllerId)) {
            return;
        }

        List<UUID> order = apnapOrder(gameData);
        offerNextOrReturn(gameData, sourceCard, sourcePermanentId,
                discarded.getId(), controllerId, lifeCost, order);
    }

    private void offerNextOrReturn(GameData gameData, Card sourceCard, UUID sourcePermanentId,
                                   UUID discardedCardId, UUID returnControllerId, int lifeCost,
                                   List<UUID> payerIds) {
        for (int i = 0; i < payerIds.size(); i++) {
            UUID payerId = payerIds.get(i);
            if (!canPayLife(gameData, payerId, lifeCost)) {
                continue;
            }
            var pendingEffect = new DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect(
                    lifeCost, discardedCardId, returnControllerId,
                    payerIds.subList(i + 1, payerIds.size()));
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    sourceCard,
                    payerId,
                    List.of(pendingEffect),
                    "Pay " + lifeCost + " life to prevent the discarded creature from returning?",
                    discardedCardId,
                    null,
                    sourcePermanentId));
            return;
        }

        returnDiscardedCreature(gameData, discardedCardId, returnControllerId, sourceCard);
    }

    public void returnDiscardedCreature(GameData gameData, UUID discardedCardId,
                                        UUID returnControllerId, Card sourceCard) {
        Card discarded = gameQueryService.findCardInGraveyardById(gameData, discardedCardId);
        if (discarded == null) {
            return;
        }
        permanentRemovalService.removeCardFromGraveyardById(gameData, discardedCardId);
        graveyardReturnSupport.putCardOntoBattlefield(gameData, returnControllerId, discarded);
        gameLogService.append(gameData, GameLog.cardTextCard(
                discarded, " returns to the battlefield with ", sourceCard, "."));
    }

    public boolean canPayLife(GameData gameData, UUID playerId, int lifeCost) {
        return gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameQueryService.canPlayerLoseLife(gameData, playerId)
                && gameData.getLife(playerId) >= lifeCost;
    }

    public List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
