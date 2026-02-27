package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExileResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @HandlesEffect(ExileTargetPermanentEffect.class)
    void resolveExileTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        permanentRemovalService.removePermanentToExile(gameData, target);
        String logEntry = target.getCard().getName() + " is exiled.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is exiled by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    @HandlesEffect(ExileTargetPermanentAndReturnAtEndStepEffect.class)
    void resolveExileTargetPermanentAndReturnAtEndStep(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        Card card = target.getOriginalCard();
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);

        permanentRemovalService.removePermanentToExile(gameData, target);

        String logEntry = card.getName() + " is exiled. It will return at the beginning of the next end step.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {}; will return at next end step",
                gameData.id, entry.getCard().getName(), card.getName());

        gameData.pendingExileReturns.add(new PendingExileReturn(card, ownerId));

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    @HandlesEffect(ExileSelfAndReturnAtEndStepEffect.class)
    void resolveExileSelfAndReturnAtEndStep(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        Card card = source.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, source);

        String logEntry = card.getName() + " is exiled. It will return at the beginning of the next end step.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is exiled and will return at next end step",
                gameData.id, card.getName());

        gameData.pendingExileReturns.add(new PendingExileReturn(card, entry.getControllerId()));

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    @HandlesEffect(ImprintDyingCreatureEffect.class)
    void resolveImprintDyingCreature(GameData gameData, StackEntry entry, ImprintDyingCreatureEffect effect) {
        UUID dyingCardId = effect.dyingCardId();
        if (dyingCardId == null) return;

        // Find the source permanent (Mimic Vat) on the battlefield
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (sourcePermanent == null) {
            log.info("Game {} - Mimic Vat no longer on battlefield, imprint fizzles", gameData.id);
            return;
        }

        // Find the dying card in any graveyard
        Card dyingCard = null;
        UUID graveyardOwnerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card c : graveyard) {
                if (c.getId().equals(dyingCardId)) {
                    dyingCard = c;
                    graveyardOwnerId = playerId;
                    break;
                }
            }
            if (dyingCard != null) break;
        }

        if (dyingCard == null) {
            log.info("Game {} - Dying card no longer in any graveyard, imprint fizzles", gameData.id);
            return;
        }

        // Return previously imprinted card to its owner's graveyard
        Card previouslyImprinted = sourcePermanent.getCard().getImprintedCard();
        if (previouslyImprinted != null) {
            // Find and remove from whichever exile zone it's in, tracking the owner
            UUID previousOwnerId = null;
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Card> exile = gameData.playerExiledCards.get(playerId);
                if (exile != null && exile.remove(previouslyImprinted)) {
                    previousOwnerId = playerId;
                    break;
                }
            }
            // Return to owner's graveyard (the player whose exile zone it was in)
            UUID returnToId = previousOwnerId != null ? previousOwnerId : entry.getControllerId();
            gameHelper.addCardToGraveyard(gameData, returnToId, previouslyImprinted);
            String returnLog = previouslyImprinted.getName() + " returns to its owner's graveyard from exile.";
            gameBroadcastService.logAndBroadcast(gameData, returnLog);
            log.info("Game {} - Previously imprinted {} returned to graveyard", gameData.id, previouslyImprinted.getName());
        }

        // Remove dying card from graveyard
        gameData.playerGraveyards.get(graveyardOwnerId).remove(dyingCard);

        // Exile the dying card (add to card owner's exile zone)
        gameData.playerExiledCards.get(graveyardOwnerId).add(dyingCard);

        // Set as imprinted on the source permanent
        sourcePermanent.getCard().setImprintedCard(dyingCard);

        String logMsg = dyingCard.getName() + " is exiled and imprinted on " + sourcePermanent.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} imprinted on {}", gameData.id, dyingCard.getName(), sourcePermanent.getCard().getName());
    }

    @HandlesEffect(ExileFromHandToImprintEffect.class)
    void resolveExileFromHandToImprint(GameData gameData, StackEntry entry, ExileFromHandToImprintEffect effect) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (sourcePermanent == null) {
            log.info("Game {} - Source permanent no longer on battlefield, imprint from hand fizzles", gameData.id);
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            log.info("Game {} - Controller has no cards in hand, imprint from hand skipped", gameData.id);
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (gameQueryService.matchesCardPredicate(hand.get(i), effect.filter(), null)) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            log.info("Game {} - {} has no matching cards in hand, imprint from hand skipped", gameData.id, playerName);
            return;
        }

        playerInputService.beginImprintFromHandChoice(gameData, controllerId, validIndices,
                "Choose " + effect.description() + " from your hand to exile and imprint.", sourcePermanent.getId());
    }
}

