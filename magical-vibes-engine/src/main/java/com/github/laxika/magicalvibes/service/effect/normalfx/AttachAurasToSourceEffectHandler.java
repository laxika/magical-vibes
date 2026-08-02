package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link AttachAurasToSourceEffect}: the controller picks Auras out of one pool and each
 * pick is attached to the source permanent.
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachAurasToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent host = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (host == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        AttachAurasToSourceEffect auraEffect = (AttachAurasToSourceEffect) effect;
        List<UUID> choosableIds = choosableAuraCardIds(gameData, host, controllerId,
                auraEffect.includeBattlefield(), auraEffect.includeLibrary());
        if (choosableIds.isEmpty()) {
            if (auraEffect.includeLibrary() && canSearchLibrary(gameData, controllerId)) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            gameLogService.append(gameData,
                    GameLog.cardThen(host.getCard(), " has no Auras it could gain."));
            return;
        }

        playerInputService.beginAttachAurasChoice(gameData, new PendingInteraction.AttachAurasChoice(
                controllerId, choosableIds, host.getId(), host.getCard().getName(),
                auraEffect.maxCount()));
    }

    /**
     * Move every chosen Aura onto the host: battlefield Auras are reattached, graveyard and hand
     * Aura cards enter the battlefield already attached. Picks are applied in begin-time order.
     */
    public void completeChoice(GameData gameData, List<UUID> chosenCardIds,
            PendingInteraction.AttachAurasChoice interaction) {
        Permanent host = gameQueryService.findPermanentById(gameData, interaction.hostPermanentId());
        if (host == null) {
            return;
        }

        Set<UUID> chosen = new LinkedHashSet<>(chosenCardIds);
        UUID controllerId = interaction.playerId();
        boolean movedAny = false;
        for (UUID cardId : interaction.validCardIds()) {
            if (!chosen.contains(cardId)) {
                continue;
            }
            movedAny |= attachFromBattlefield(gameData, host, cardId)
                    || attachFromGraveyard(gameData, host, controllerId, cardId)
                    || attachFromHand(gameData, host, controllerId, cardId)
                    || attachFromLibrary(gameData, host, controllerId, cardId);
        }

        if (movedAny) {
            // A control Aura (e.g. Control Magic) that moved grants control of the host to its controller.
            creatureControlService.recomputeControl(gameData, host);
        }
    }

    /** Aura card ids that could enchant the host, in battlefield, graveyard, hand, and library order. */
    private List<UUID> choosableAuraCardIds(GameData gameData, Permanent host, UUID controllerId,
            boolean includeBattlefield, boolean includeLibrary) {
        List<UUID> ids = new ArrayList<>();
        if (includeBattlefield) {
            gameData.forEachPermanent((playerId, permanent) -> {
                if (!permanent.getCard().isAura() || host.getId().equals(permanent.getAttachedTo())) {
                    return;
                }
                UUID auraControllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (auraAttachmentService.canEnchant(gameData, permanent.getCard(), auraControllerId, host)) {
                    ids.add(permanent.getCard().getId());
                }
            });
        }
        addEnchantableAuraCards(gameData, host, controllerId,
                gameData.playerGraveyards.getOrDefault(controllerId, List.of()), ids);
        addEnchantableAuraCards(gameData, host, controllerId,
                gameData.playerHands.getOrDefault(controllerId, List.of()), ids);
        if (includeLibrary && canSearchLibrary(gameData, controllerId)) {
            addEnchantableAuraCards(gameData, host, controllerId,
                    gameData.playerDecks.getOrDefault(controllerId, List.of()), ids);
        }
        return ids;
    }

    private boolean canSearchLibrary(GameData gameData, UUID controllerId) {
        return !librarySearchSupport.isSearchPrevented(gameData, controllerId);
    }

    private void addEnchantableAuraCards(GameData gameData, Permanent host, UUID controllerId,
            List<Card> cards, List<UUID> ids) {
        for (Card card : cards) {
            if (card.isAura() && auraAttachmentService.canEnchant(gameData, card, controllerId, host)) {
                ids.add(card.getId());
            }
        }
    }

    private boolean attachFromBattlefield(GameData gameData, Permanent host, UUID cardId) {
        Permanent aura = findAuraPermanent(gameData, cardId);
        if (aura == null) {
            return false;
        }
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(host.getId());
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData,
                GameLog.cardTextCard(aura.getCard(), " is now attached to ", host.getCard(), "."));
        log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(),
                host.getCard().getName());
        return true;
    }

    private boolean attachFromGraveyard(GameData gameData, Permanent host, UUID controllerId, UUID cardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        Card card = findCard(graveyard, cardId);
        if (card == null) {
            return false;
        }
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            graveyard.remove(card);
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
            putAuraOntoBattlefieldAttached(gameData, host, controllerId, card, "graveyard");
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        return true;
    }

    private boolean attachFromHand(GameData gameData, Permanent host, UUID controllerId, UUID cardId) {
        List<Card> hand = gameData.playerHands.get(controllerId);
        Card card = findCard(hand, cardId);
        if (card == null) {
            return false;
        }
        hand.remove(card);
        putAuraOntoBattlefieldAttached(gameData, host, controllerId, card, "hand");
        return true;
    }

    private boolean attachFromLibrary(GameData gameData, Permanent host, UUID controllerId, UUID cardId) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        Card card = findCard(library, cardId);
        if (card == null) {
            return false;
        }
        library.remove(card);
        putAuraOntoBattlefieldAttached(gameData, host, controllerId, card, "library");
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        return true;
    }

    private void putAuraOntoBattlefieldAttached(GameData gameData, Permanent host, UUID controllerId,
            Card card, String zoneName) {
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(host.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, aura);
        gameLogService.append(gameData, GameLog.builder()
                .card(card)
                .text(" enters from " + zoneName + " attached to ")
                .card(host.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} enters from {} attached to {}", gameData.id, card.getName(), zoneName,
                host.getCard().getName());
    }

    private Permanent findAuraPermanent(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getId().equals(cardId)) {
                found.add(permanent);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    private Card findCard(List<Card> cards, UUID cardId) {
        if (cards == null) {
            return null;
        }
        return cards.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
    }
}
