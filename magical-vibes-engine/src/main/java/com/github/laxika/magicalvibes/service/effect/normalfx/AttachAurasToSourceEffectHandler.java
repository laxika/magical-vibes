package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link AttachAurasToSourceEffect}: the controller picks eligible attachments out of one
 * pool and each pick is attached to the source permanent.
 *
 * <p>Only Auras that could legally enchant the source are offered, so no pick can fail to move
 * (CR 701.3a). Auras already attached to the source aren't offered: attaching something to what
 * it's already attached to does nothing (CR 701.3b).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachAurasToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final GraveyardService graveyardService;
    private final LibrarySearchSupport librarySearchSupport;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachAurasToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AttachAurasToSourceEffect auraEffect = (AttachAurasToSourceEffect) effect;
        UUID hostId = auraEffect.hostPermanentId() == null
                ? entry.getSourcePermanentId() : auraEffect.hostPermanentId();
        Permanent host = gameQueryService.findPermanentById(gameData, hostId);
        if (host == null && !auraEffect.includeEquipment()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<UUID> choosableIds = choosableAttachmentCardIds(gameData, host, controllerId,
                auraEffect.includeBattlefield(), auraEffect.includeLibrary(), auraEffect.includeEquipment());
        if (choosableIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " has no "
                            + (auraEffect.includeEquipment() ? "Auras or Equipment" : "Auras")
                            + " it could gain."));
            return;
        }

        playerInputService.beginAttachAurasChoice(gameData, new PendingInteraction.AttachAurasChoice(
                controllerId, choosableIds, hostId, entry.getCard().getName(),
                auraEffect.maxCount(), auraEffect.includeEquipment()));
    }

    /**
     * Move every chosen attachment onto the host: battlefield attachments are reattached, while
     * graveyard and hand cards enter the battlefield already attached. Picks are applied in
     * as one event, with timestamps ordered by the active player and then the other players.
     */
    public void completeChoice(GameData gameData, List<UUID> chosenCardIds,
            PendingInteraction.AttachAurasChoice interaction) {
        Permanent host = gameQueryService.findPermanentById(gameData, interaction.hostPermanentId());
        if (host == null && !interaction.includeEquipment()) {
            return;
        }

        Set<UUID> chosen = new LinkedHashSet<>(chosenCardIds);
        UUID controllerId = interaction.playerId();
        List<UUID> battlefieldChoices = new ArrayList<>();
        List<UUID> activePlayerOrder = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = activePlayerOrder.indexOf(gameData.activePlayerId);
        if (activeIndex >= 0) {
            java.util.Collections.rotate(activePlayerOrder, -activeIndex);
        }
        for (UUID attachmentControllerId : activePlayerOrder) {
            for (UUID cardId : interaction.validCardIds()) {
                Permanent attachment = findAttachmentPermanent(gameData, cardId);
                if (chosen.contains(cardId) && attachment != null
                        && attachmentControllerId.equals(gameQueryService.findPermanentController(gameData, attachment.getId()))
                        && canAttach(gameData, attachment, attachmentControllerId, host)) {
                    battlefieldChoices.add(cardId);
                }
            }
        }
        boolean movedAny = false;
        for (UUID cardId : battlefieldChoices) {
            movedAny |= attachFromBattlefield(gameData, host, cardId, true);
        }
        for (UUID cardId : interaction.validCardIds()) {
            if (!chosen.contains(cardId) || findAttachmentPermanent(gameData, cardId) != null) {
                continue;
            }
            movedAny |= attachFromGraveyard(gameData, host, controllerId, cardId)
                    || attachFromHand(gameData, host, controllerId, cardId)
                    || attachFromLibrary(gameData, host, controllerId, cardId);
        }

        if (movedAny && host != null) {
            // A control Aura (e.g. Control Magic) that moved grants control of the host to its controller.
            creatureControlService.recomputeControl(gameData, host);
        }
    }

    /** Card ids that could attach to the host, in battlefield, graveyard, hand, and library order. */
    private List<UUID> choosableAttachmentCardIds(GameData gameData, Permanent host, UUID controllerId,
            boolean includeBattlefield, boolean includeLibrary, boolean includeEquipment) {
        List<UUID> ids = new ArrayList<>();
        if (includeBattlefield && host != null) {
            gameData.forEachPermanent((playerId, permanent) -> {
                if ((!permanent.getCard().isAura()
                        && !(includeEquipment && isEquipment(permanent.getCard())))
                        || host.getId().equals(permanent.getAttachedTo())) {
                    return;
                }
                UUID attachmentControllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (canAttach(gameData, permanent, attachmentControllerId, host)) {
                    ids.add(permanent.getCard().getId());
                }
            });
        }
        addEnchantableCards(gameData, host, controllerId,
                gameData.playerGraveyards.getOrDefault(controllerId, List.of()), ids, includeEquipment);
        addEnchantableCards(gameData, host, controllerId,
                gameData.playerHands.getOrDefault(controllerId, List.of()), ids, includeEquipment);
        if (includeLibrary && canSearchLibrary(gameData, controllerId)) {
            List<Card> library = gameData.playerDecks.getOrDefault(controllerId, List.of());
            int searchLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId);
            if (searchLimit > 0) {
                library = library.subList(0, Math.min(searchLimit, library.size()));
            }
            addEnchantableCards(gameData, host, controllerId,
                    library, ids, includeEquipment);
        }
        return ids;
    }

    private boolean canSearchLibrary(GameData gameData, UUID controllerId) {
        return !librarySearchSupport.isSearchPrevented(gameData, controllerId);
    }

    private void addEnchantableCards(GameData gameData, Permanent host, UUID controllerId,
            List<Card> cards, List<UUID> ids, boolean includeEquipment) {
        for (Card card : cards) {
            if ((host != null && card.isAura() && auraAttachmentService.canEnchant(gameData, card, controllerId, host))
                    || (includeEquipment && isEquipment(card)
                    && (host == null || equipSupport.canAttachEquipment(gameData, new Permanent(card), host)))) {
                ids.add(card.getId());
            }
        }
    }

    private boolean attachFromBattlefield(GameData gameData, Permanent host, UUID cardId, boolean attachmentWasLegal) {
        Permanent attachment = findAttachmentPermanent(gameData, cardId);
        if (attachment == null) {
            return false;
        }
        UUID attachmentControllerId = gameQueryService.findPermanentController(gameData, attachment.getId());
        if (!attachmentWasLegal && !canAttach(gameData, attachment, attachmentControllerId, host)) {
            return false;
        }
        if (attachment.getCard().isAura()) {
            gameData.expireFloatingEffectsForUnattachedSource(attachment.getId());
            attachment.setAttachedTo(host.getId());
            // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
            attachment.setTimestamp(gameData.nextTimestamp());
        } else if (!equipSupport.attachEquipment(gameData, attachment, host)) {
            return false;
        }
        gameLogService.append(gameData,
                GameLog.cardTextCard(attachment.getCard(), " is now attached to ", host.getCard(), "."));
        log.info("Game {} - {} reattached to {}", gameData.id, attachment.getCard().getName(),
                host.getCard().getName());
        return true;
    }

    private boolean attachFromGraveyard(GameData gameData, Permanent host, UUID controllerId, UUID cardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        Card card = findCard(graveyard, cardId);
        if (card == null || (host == null && !isEquipment(card)) || (card.isAura()
                && !auraAttachmentService.canEnchant(gameData, card, controllerId, host))) {
            return false;
        }
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            graveyard.remove(card);
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
            putAttachmentOntoBattlefieldAttached(gameData, host, controllerId, card, "graveyard", Zone.GRAVEYARD);
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        return true;
    }

    private boolean attachFromHand(GameData gameData, Permanent host, UUID controllerId, UUID cardId) {
        List<Card> hand = gameData.playerHands.get(controllerId);
        Card card = findCard(hand, cardId);
        if (card == null || (host == null && !isEquipment(card)) || (card.isAura()
                && !auraAttachmentService.canEnchant(gameData, card, controllerId, host))) {
            return false;
        }
        hand.remove(card);
        putAttachmentOntoBattlefieldAttached(gameData, host, controllerId, card, "hand", Zone.HAND);
        return true;
    }

    private boolean attachFromLibrary(GameData gameData, Permanent host, UUID controllerId, UUID cardId) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        Card card = findCard(library, cardId);
        if (card == null || (host == null && !isEquipment(card)) || (card.isAura()
                && !auraAttachmentService.canEnchant(gameData, card, controllerId, host))) {
            return false;
        }
        LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
        library.remove(card);
        putAttachmentOntoBattlefieldAttached(gameData, host, controllerId, card, "library", Zone.LIBRARY);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        return true;
    }

    private void putAttachmentOntoBattlefieldAttached(GameData gameData, Permanent host, UUID controllerId,
            Card card, String zoneName, Zone origin) {
        Permanent attachment = new Permanent(card, origin);
        attachment.setAttachedTo(host == null ? null : host.getId());
        if (origin == Zone.GRAVEYARD) {
            attachment.setEnteredFromGraveyardOwnerId(controllerId);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, attachment);
        if (host != null && isEquipment(card) && gameQueryService.findPermanentById(gameData, attachment.getId()) != null) {
            equipSupport.notifyEquipmentAttached(gameData, attachment, null);
        }
        if (host == null) {
            gameLogService.append(gameData, GameLog.cardThen(card, " enters from " + zoneName + "."));
            return;
        }
        gameLogService.append(gameData, GameLog.builder()
                .card(card)
                .text(" enters from " + zoneName + " attached to ")
                .card(host.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} enters from {} attached to {}", gameData.id, card.getName(), zoneName,
                host.getCard().getName());
    }

    private Permanent findAttachmentPermanent(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getId().equals(cardId)) {
                found.add(permanent);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    private boolean canAttach(GameData gameData, Permanent attachment, UUID attachmentControllerId,
            Permanent host) {
        if (attachment.getCard().isAura()) {
            return auraAttachmentService.canEnchant(gameData, attachment.getCard(), attachmentControllerId, host);
        }
        return isEquipment(attachment.getCard())
                && equipSupport.canAttachEquipment(gameData, attachment, host);
    }

    private boolean isEquipment(Card card) {
        return card.getSubtypes().contains(CardSubtype.EQUIPMENT);
    }

    private Card findCard(List<Card> cards, UUID cardId) {
        if (cards == null) {
            return null;
        }
        return cards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
    }
}
