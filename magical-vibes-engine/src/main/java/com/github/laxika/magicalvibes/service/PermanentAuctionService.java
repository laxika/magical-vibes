package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Runs a permanent auction (e.g. Thieves' Auction): exile every nontoken permanent into a shared
 * pool, then (starting with the controller, in turn order and wrapping) have each player choose one
 * exiled card and put it onto the battlefield tapped under their control, repeating until the pool
 * empties or no card left in it can legally enter. Each card enters as it is chosen, so its as-enters
 * choices are made before the next player picks; an Aura enters attached to a permanent its controller
 * chooses (it does not target) and stays exiled when nothing can be enchanted. The auction state travels on the {@link PendingInteraction.PermanentAuctionChoice}
 * record; each answered pick begins a fresh record for the next chooser.
 */
@Slf4j
@Component
public class PermanentAuctionService {

    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameMutationCoordinator mutationCoordinator;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;
    private final AuraAttachmentService auraAttachmentService;
    private final PlayerInputService playerInputService;
    private final CreatureControlService creatureControlService;

    // @Lazy mirrors ExileSupport: the completion epilogue resumes effect resolution, which reaches
    // this service again through the effect-handler registry.
    public PermanentAuctionService(PermanentRemovalService permanentRemovalService,
                                   BattlefieldEntryService battlefieldEntryService,
                                   GameLogService gameLogService,
                                   GameMutationCoordinator mutationCoordinator,
                                   InteractionHandlerRegistry interactionHandlerRegistry,
                                   @Lazy InputCompletionService inputCompletionService,
                                   AuraAttachmentService auraAttachmentService,
                                   PlayerInputService playerInputService,
                                   CreatureControlService creatureControlService) {
        this.permanentRemovalService = permanentRemovalService;
        this.battlefieldEntryService = battlefieldEntryService;
        this.gameLogService = gameLogService;
        this.mutationCoordinator = mutationCoordinator;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.inputCompletionService = inputCompletionService;
        this.auraAttachmentService = auraAttachmentService;
        this.playerInputService = playerInputService;
        this.creatureControlService = creatureControlService;
    }

    /** Exiles all nontoken permanents and begins the auction with the controller. */
    public void beginAuction(GameData gameData, UUID controllerId, String sourceName) {
        List<Permanent> toExile = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (!perm.getCard().isToken()) {
                    toExile.add(perm);
                }
            }
        });

        List<Card> pool = new ArrayList<>();
        for (Permanent perm : toExile) {
            List<Card> cards = perm.cardsLeavingBattlefield();
            if (permanentRemovalService.removePermanentToExile(gameData, perm)) {
                for (Card card : cards) {
                    pool.add(card);
                    gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
                }
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (pool.isEmpty()) {
            // Nothing to auction, and nothing has paused: this still runs inside the spell's
            // resolution, so the effect loop and StackResolutionService carry it the rest of the
            // way. Auto-passing from here would advance the game with the spell still on the stack.
            gameLogService.append(gameData, GameLog.text(sourceName + " exiles no permanents."));
            return;
        }

        // Turn order starting with the controller.
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int start = ordered.indexOf(controllerId);
        List<UUID> playerOrder = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            playerOrder.add(ordered.get((start + i) % ordered.size()));
        }

        beginPick(gameData, controllerId, pool, playerOrder, new ArrayList<>());
    }

    /** Applies one player's auction pick and advances the auction (or finishes it). */
    public void applyPick(GameData gameData, Player player, List<UUID> cardIds) {
        PendingInteraction.PermanentAuctionChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentAuctionChoice.class);
        if (choice == null || !player.getId().equals(choice.choosingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Card> pool = new ArrayList<>(choice.pool());
        UUID chosenId = cardIds == null ? null
                : cardIds.stream()
                    .filter(id -> pool.stream().anyMatch(c -> c.getId().equals(id)))
                    .findFirst().orElse(null);
        if (chosenId == null) {
            // Mandatory pick — an empty/invalid selection re-prompts the same player.
            log.warn("Game {} - {} sent an invalid auction pick, re-prompting", gameData.id, player.getUsername());
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentAuctionChoice(
                    choice.choosingPlayerId(), pool, choice.playerOrder(), choice.placed()));
            return;
        }

        Card chosen = pool.stream().filter(c -> c.getId().equals(chosenId)).findFirst().orElseThrow();
        UUID chooserId = player.getId();
        gameData.interaction.clearAwaitingInput();

        PendingInteraction.PermanentAuctionChoice next;
        if (chosen.isAura()) {
            List<UUID> hosts = auraHosts(gameData, chooserId, chosen);
            if (hosts.isEmpty()) {
                // Nothing it can enchant: it stays exiled and can still be chosen later (ruling 2005-08-01).
                gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " cannot put ", chosen,
                        " onto the battlefield; it remains exiled."));
                next = nextPick(chooserId, pool, choice);
            } else {
                pool.removeIf(c -> c.getId().equals(chosenId));
                next = nextPick(chooserId, pool, choice);
                if (hosts.size() > 1) {
                    gameData.pendingAuctionNext = next;
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.PermanentAuctionAuraHost(chooserId, chosenId));
                    playerInputService.beginPermanentChoice(gameData, chooserId, hosts,
                            "Choose a permanent to enchant with " + chosen.getName() + ".");
                    return;
                }
                enterPermanent(gameData, chooserId, chosen, gameQueryHost(gameData, hosts.getFirst()));
            }
        } else {
            pool.removeIf(c -> c.getId().equals(chosenId));
            enterPermanent(gameData, chooserId, chosen, null);
            next = nextPick(chooserId, pool, choice);
        }
        continueAfterEntry(gameData, next);
    }

    /** Completes a pick whose Aura had several legal hosts: it enters attached to the chosen permanent. */
    public void completeAuraHostChoice(GameData gameData, UUID hostId,
                                       PermanentChoiceContext.PermanentAuctionAuraHost context) {
        var exiledAura = gameData.findExiledCard(context.auraCardId());
        Card aura = exiledAura == null ? null : exiledAura.card();
        Permanent host = gameQueryHost(gameData, hostId);
        PendingInteraction.PermanentAuctionChoice next = gameData.pendingAuctionNext;
        gameData.pendingAuctionNext = null;
        if (aura != null && host != null
                && auraAttachmentService.canEnchant(gameData, aura, context.controllerId(), host)) {
            enterPermanent(gameData, context.controllerId(), aura, host);
        }
        continueAfterEntry(gameData, next);
    }

    /** Continues the auction once an as-enters or Aura host choice for the previous pick has been answered. */
    public void resumeAuction(GameData gameData) {
        PendingInteraction.PermanentAuctionChoice next = gameData.pendingAuctionNext;
        gameData.pendingAuctionNext = null;
        if (next != null) {
            continueAfterEntry(gameData, next);
        }
    }

    private void enterPermanent(GameData gameData, UUID chooserId, Card card, Permanent attachedTo) {
        gameData.removeFromExile(card.getId());
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        permanent.tap();
        if (attachedTo != null) {
            permanent.setAttachedTo(attachedTo.getId());
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, chooserId, permanent);
        creatureControlService.reconcileControl(gameData);
        String chooserName = gameData.playerIdToName.get(chooserId);
        gameLogService.append(gameData, GameLog.textCardText(chooserName + " puts ", card,
                " onto the battlefield tapped."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, chooserId, card, null, false);
    }

    private Permanent gameQueryHost(GameData gameData, UUID hostId) {
        Permanent[] found = new Permanent[1];
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(p -> p.getId().equals(hostId)).findFirst().ifPresent(p -> found[0] = p));
        return found[0];
    }

    private List<UUID> auraHosts(GameData gameData, UUID chooserId, Card aura) {
        List<UUID> hosts = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (auraAttachmentService.canEnchant(gameData, aura, chooserId, perm)) {
                    hosts.add(perm.getId());
                }
            }
        });
        return hosts;
    }

    /** The prompt after a pick, or null when no card left in the pool can legally enter. */
    private PendingInteraction.PermanentAuctionChoice nextPick(UUID chooserId, List<Card> pool,
                                                               PendingInteraction.PermanentAuctionChoice previous) {
        List<UUID> order = previous.playerOrder();
        UUID next = order.get((order.indexOf(chooserId) + 1) % order.size());
        return new PendingInteraction.PermanentAuctionChoice(next, pool, order, previous.placed());
    }

    private void continueAfterEntry(GameData gameData, PendingInteraction.PermanentAuctionChoice next) {
        if (gameData.interaction.isAwaitingInput()) {
            // An as-enters choice must be answered first; the auction resumes from InputCompletionService.
            gameData.pendingAuctionNext = next;
            return;
        }
        if (next != null && canAnyEnter(gameData, next)) {
            mutationCoordinator.invalidateAllPlayerViews(gameData);
            interactionHandlerRegistry.begin(gameData, next);
            return;
        }
        finishAuction(gameData);
    }

    private boolean canAnyEnter(GameData gameData, PendingInteraction.PermanentAuctionChoice next) {
        return next.pool().stream().anyMatch(card -> !card.isAura()
                || !auraHosts(gameData, next.choosingPlayerId(), card).isEmpty());
    }

    private void beginPick(GameData gameData, UUID chooserId, List<Card> pool, List<UUID> playerOrder,
                           List<PendingInteraction.PermanentAuctionPlacement> placed) {
        mutationCoordinator.invalidateAllPlayerViews(gameData);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PermanentAuctionChoice(chooserId, pool, playerOrder, placed));
    }

    /**
     * The auction is over, so its pick ends the spell's resolution. The picks were answered
     * mid-resolution, so this must end through {@link InputCompletionService}: it resumes the stack
     * entry parked in {@code GameData.pendingEffectResolutionEntry}, and leaving it parked would wedge
     * {@code GameData.deferPlayerLossCheck} for the rest of the game.
     */
    private void finishAuction(GameData gameData) {
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
