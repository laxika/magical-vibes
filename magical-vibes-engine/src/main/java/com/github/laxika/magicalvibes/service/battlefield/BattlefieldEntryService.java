package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Coordinates permanent placement, as-entry interactions, and enter-the-battlefield trigger collection.
 *
 * <p>The public methods remain as a compatibility boundary while the rules-sensitive phases are owned
 * by focused collaborators.
 */
@Component
public class BattlefieldEntryService {

    private final BattlefieldPlacementService placementService;
    private final AsEntersInteractionService interactionService;
    private final EtbTriggerService triggerService;

    public BattlefieldEntryService(BattlefieldPlacementService placementService,
                                   AsEntersInteractionService interactionService,
                                   EtbTriggerService triggerService) {
        this.placementService = placementService;
        this.interactionService = interactionService;
        this.triggerService = triggerService;
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent) {
        place(gameData, controllerId, permanent, placementService.snapshotEnterTappedTypes(gameData),
                List.of(), 0, false, List.of());
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                            Set<CardType> enterTappedTypes) {
        place(gameData, controllerId, permanent, enterTappedTypes, List.of(), 0, false, List.of());
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                            Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered) {
        place(gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered,
                0, false, List.of());
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                            int xValue, boolean kicked) {
        place(gameData, controllerId, permanent, placementService.snapshotEnterTappedTypes(gameData),
                List.of(), xValue, kicked, List.of());
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                            int xValue, boolean kicked, List<String> repeatedAdditionalCosts) {
        place(gameData, controllerId, permanent, placementService.snapshotEnterTappedTypes(gameData),
                List.of(), xValue, kicked, repeatedAdditionalCosts);
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                            Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered,
                                            int xValue, boolean kicked) {
        place(gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered,
                xValue, kicked, List.of());
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                            Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered,
                                            int xValue, boolean kicked, List<String> repeatedAdditionalCosts) {
        place(gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered,
                xValue, kicked, repeatedAdditionalCosts);
    }

    private void place(GameData gameData, UUID controllerId, Permanent permanent,
                       Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered,
                       int xValue, boolean kicked, List<String> repeatedAdditionalCosts) {
        placementService.place(gameData, new BattlefieldEntryRequest(controllerId, permanent,
                enterTappedTypes, simultaneouslyEntered, xValue, kicked, repeatedAdditionalCosts));
    }

    public UUID resolveEnteringController(GameData gameData, UUID controllerId, Permanent permanent) {
        return placementService.resolveEnteringController(gameData, controllerId, permanent);
    }

    public Set<CardType> snapshotEnterTappedTypes(GameData gameData) {
        return placementService.snapshotEnterTappedTypes(gameData);
    }

    public void completeSacrificePermanentToEnter(
            GameData gameData, UUID controllerId, Permanent permanent, boolean sacrificed) {
        placementService.completeSacrificePermanentToEnter(gameData, controllerId, permanent, sacrificed);
    }

    public void completeSacrificePermanentsToEnter(
            GameData gameData, UUID controllerId, Permanent permanent, boolean sacrificed) {
        placementService.completeSacrificePermanentsToEnter(
                gameData, controllerId, permanent, sacrificed);
    }

    public void completeDiscardCardToEnter(
            GameData gameData, UUID controllerId, Permanent permanent, boolean discarded) {
        placementService.completeDiscardCardToEnter(gameData, controllerId, permanent, discarded);
    }

    public void applyDeferredEnterWithCounters(GameData gameData, UUID controllerId, Permanent permanent) {
        placementService.applyDeferredEnterWithCounters(gameData, controllerId, permanent);
    }

    public boolean permanentWouldHaveSubtype(
            GameData gameData, Permanent entering, UUID controllerId, CardSubtype subtype) {
        return placementService.permanentWouldHaveSubtype(
                gameData, entering, controllerId, List.of(), subtype);
    }

    public boolean permanentWouldHaveSubtype(
            GameData gameData, Permanent entering, UUID controllerId,
            List<Permanent> simultaneouslyEntered, CardSubtype subtype) {
        return placementService.permanentWouldHaveSubtype(
                gameData, entering, controllerId, simultaneouslyEntered, subtype);
    }

    public void handleCreatureEnteredBattlefield(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        interactionService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, targetId, wasCastFromHand);
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        triggerService.checkAllyTokenEntersTriggers(gameData, controllerId, count);
    }

    public void handleCreatureEnteredBattlefield(
            GameData gameData, UUID controllerId, Card card, UUID targetId,
            boolean wasCastFromHand, int etbMode) {
        interactionService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode);
    }

    public void handleCreatureEnteredBattlefield(
            GameData gameData, UUID controllerId, Card card, UUID targetId,
            boolean wasCastFromHand, int etbMode, boolean kicked) {
        interactionService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked);
    }

    public void handleCreatureEnteredBattlefield(
            GameData gameData, UUID controllerId, Card card, UUID targetId,
            boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
        interactionService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, targetIds);
    }

    public void handleCreatureEnteredBattlefield(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand,
            int etbMode, int xValue, boolean kicked, List<UUID> targetIds) {
        interactionService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds);
    }

    public void handleCreatureEnteredBattlefield(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand,
            int etbMode, int xValue, boolean kicked, List<UUID> targetIds,
            List<String> repeatedAdditionalCosts) {
        interactionService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, repeatedAdditionalCosts);
    }

    public void handleCreatureEnteredBattlefield(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand,
            int etbMode, int xValue, boolean kicked, List<UUID> targetIds,
            List<String> repeatedAdditionalCosts, List<UUID> convokeCreatureIds) {
        interactionService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode, xValue, kicked,
                targetIds, repeatedAdditionalCosts, convokeCreatureIds);
    }

    public void applyAsEntersExileCounters(
            GameData gameData, UUID controllerId, UUID enteringPermanentId,
            int exiledCount, int countersPerCard) {
        interactionService.applyAsEntersExileCounters(
                gameData, controllerId, enteringPermanentId, exiledCount, countersPerCard);
    }

    public void applyAsEntersChosenCounterType(
            GameData gameData, UUID controllerId, UUID enteringPermanentId,
            CounterType counterType, int count) {
        interactionService.applyAsEntersChosenCounterType(
                gameData, controllerId, enteringPermanentId, counterType, count);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        triggerService.processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId,
            boolean wasCastFromHand, List<UUID> targetIds) {
        triggerService.processCreatureETBEffects(
                gameData, controllerId, card, targetId, wasCastFromHand, targetIds);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId,
            boolean wasCastFromHand, int etbMode) {
        triggerService.processCreatureETBEffects(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId,
            boolean wasCastFromHand, int etbMode, boolean kicked) {
        triggerService.processCreatureETBEffects(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked);
    }

    public void processLandETBEffects(GameData gameData, UUID controllerId, Card card) {
        triggerService.processLandETBEffects(gameData, controllerId, card);
    }

    public void processFaceDownCreatureETBTriggers(GameData gameData, UUID controllerId, Card card) {
        triggerService.processFaceDownCreatureETBTriggers(gameData, controllerId, card);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand,
            int etbMode, boolean kicked, List<UUID> targetIds) {
        triggerService.processCreatureETBEffects(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, targetIds);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand,
            int etbMode, int xValue, boolean kicked, List<UUID> targetIds) {
        triggerService.processCreatureETBEffects(
                gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand,
            int etbMode, int xValue, boolean kicked, List<UUID> targetIds,
            List<String> repeatedAdditionalCosts) {
        triggerService.processCreatureETBEffects(
                gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, repeatedAdditionalCosts);
    }

    public void processCreatureETBEffects(
            GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand,
            int etbMode, int xValue, boolean kicked, List<UUID> targetIds,
            List<String> repeatedAdditionalCosts, List<UUID> convokeCreatureIds) {
        triggerService.processCreatureETBEffects(
                gameData, controllerId, card, targetId, wasCastFromHand, etbMode, xValue, kicked,
                targetIds, repeatedAdditionalCosts, convokeCreatureIds);
    }
}
