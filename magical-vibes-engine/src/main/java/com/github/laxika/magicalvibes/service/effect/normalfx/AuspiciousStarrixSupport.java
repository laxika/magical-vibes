package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AuspiciousStarrixAuraChoiceRequest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuspiciousStarrixSupport {

    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PlayerInputService playerInputService;

    public void begin(GameData gameData, UUID controllerId, List<Card> permanentCards) {
        var operation = gameData.auspiciousStarrixOperation;
        operation.clear();
        operation.controllerId = controllerId;
        operation.permanentCards.addAll(permanentCards);
        operation.enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        List<Permanent> existingPermanents = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> existingPermanents.add(permanent));

        for (Card card : permanentCards) {
            if (!card.isAura()) {
                continue;
            }

            List<UUID> validPermanentIds = existingPermanents.stream()
                    .filter(permanent -> auraAttachmentService.canEnchant(gameData, card, controllerId, permanent))
                    .map(Permanent::getId)
                    .toList();
            List<UUID> validPlayerIds = gameData.orderedPlayerIds.stream()
                    .filter(playerId -> auraAttachmentService.canEnchantPlayer(gameData, card, controllerId, playerId))
                    .toList();

            int validTargetCount = validPermanentIds.size() + validPlayerIds.size();
            if (validTargetCount == 1) {
                operation.auraAttachmentTargets.put(card.getId(),
                        validPermanentIds.isEmpty() ? validPlayerIds.getFirst() : validPermanentIds.getFirst());
            } else if (validTargetCount > 1) {
                operation.pendingAuraChoices.addLast(new AuspiciousStarrixAuraChoiceRequest(
                        controllerId, card, validPermanentIds, validPlayerIds));
            }
        }

        if (operation.pendingAuraChoices.isEmpty()) {
            placePermanents(gameData);
        } else {
            beginNextAuraChoice(gameData);
        }
    }

    public boolean completeAuraChoice(GameData gameData, UUID playerId, UUID targetId) {
        var operation = gameData.auspiciousStarrixOperation;
        AuspiciousStarrixAuraChoiceRequest request = operation.activeAuraChoice;
        if (request == null || !request.controllerId().equals(playerId)
                || !request.validPermanentIds().contains(targetId)
                && !request.validPlayerIds().contains(targetId)) {
            throw new IllegalStateException("Invalid Auspicious Starrix Aura choice");
        }

        Card auraCard = gameData.interaction.consumePendingAuraCard();
        gameData.interaction.consumePendingAuraOwnerId();
        if (auraCard == null) {
            throw new IllegalStateException("No pending Auspicious Starrix Aura");
        }
        operation.auraAttachmentTargets.put(auraCard.getId(), targetId);
        operation.activeAuraChoice = null;

        if (!operation.pendingAuraChoices.isEmpty()) {
            beginNextAuraChoice(gameData);
            return false;
        }

        placePermanents(gameData);
        return true;
    }

    private void beginNextAuraChoice(GameData gameData) {
        var operation = gameData.auspiciousStarrixOperation;
        AuspiciousStarrixAuraChoiceRequest request = operation.pendingAuraChoices.pollFirst();
        if (request == null) {
            placePermanents(gameData);
            return;
        }

        operation.activeAuraChoice = request;
        gameData.interaction.setPendingAuraCard(request.auraCard());
        gameData.interaction.setPendingAuraOwnerId(request.controllerId());
        playerInputService.beginAnyTargetChoice(gameData, request.controllerId(),
                request.validPermanentIds(), request.validPlayerIds(),
                "Choose a permanent or player for " + request.auraCard().getName() + " to enchant.");
    }

    private void placePermanents(GameData gameData) {
        var operation = gameData.auspiciousStarrixOperation;
        UUID controllerId = operation.controllerId;
        Set<CardType> enterTappedTypes = operation.enterTappedTypesSnapshot;
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<EnteredPermanent> enteredPermanents = new ArrayList<>();

        for (Card card : operation.permanentCards) {
            UUID attachmentTargetId = operation.auraAttachmentTargets.get(card.getId());
            if (card.isAura() && !isLegalAuraAttachment(gameData, card, controllerId, attachmentTargetId)) {
                continue;
            }
            if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.EXILE)
                    || !gameData.removeFromExile(card.getId())) {
                continue;
            }

            Permanent permanent = new Permanent(card, Zone.EXILE);
            initializePlaneswalkerLoyalty(permanent, card);
            if (attachmentTargetId != null) {
                permanent.setAttachedTo(attachmentTargetId);
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            enteredPermanents.add(new EnteredPermanent(controllerId, permanent, card));
        }

        for (EnteredPermanent entered : enteredPermanents) {
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                    gameData, entered.controllerId(), entered.permanent(), entered.card());
        }
        operation.clear();
    }

    private boolean isLegalAuraAttachment(GameData gameData, Card auraCard, UUID controllerId,
                                           UUID attachmentTargetId) {
        if (attachmentTargetId == null) {
            return false;
        }
        if (gameData.playerIds.contains(attachmentTargetId)) {
            return auraAttachmentService.canEnchantPlayer(gameData, auraCard, controllerId, attachmentTargetId);
        }
        Permanent target = gameQueryService.findPermanentById(gameData, attachmentTargetId);
        return target != null && auraAttachmentService.canEnchant(gameData, auraCard, controllerId, target);
    }

    private void initializePlaneswalkerLoyalty(Permanent permanent, Card card) {
        if (card.hasType(CardType.PLANESWALKER)) {
            permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty() != null ? card.getLoyalty() : 0);
        }
    }

    private record EnteredPermanent(UUID controllerId, Permanent permanent, Card card) {
    }
}
