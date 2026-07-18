package com.github.laxika.magicalvibes.service.aura;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Handles aura and attachment lifecycle: removing orphaned auras when their target leaves the
 * battlefield and detaching equipment. After each cleanup the CR 613.2 control state is
 * reconciled ({@link CreatureControlService#reconcileControl}) so permanents whose controlling
 * effect ended fall back to the next most recent still-active control effect (or their owner).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuraAttachmentService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * A card that was put into the graveyard as an orphaned aura, along with the controller
     * who owned it at the time. Callers use this to fire graveyard triggers after cleanup.
     */
    public record OrphanedAuraRemoval(Card card, UUID controllerId) {}

    /**
     * Removes auras whose enchanted permanent no longer exists and detaches equipment whose
     * equipped creature has left the battlefield (CR 303.4c, CR 301.5c). Orphaned auras are
     * put into their owner's graveyard; equipment simply becomes unattached. After cleanup,
     * the layer-2 control state is reconciled so permanents whose controlling effect has
     * ended change controllers accordingly.
     *
     * @param gameData the current game state
     */
    public List<OrphanedAuraRemoval> removeOrphanedAuras(GameData gameData) {
        List<OrphanedAuraRemoval> removals = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (p.isAttached()
                        && !gameData.playerIds.contains(p.getAttachedTo())
                        && gameQueryService.findPermanentById(gameData, p.getAttachedTo()) == null) {
                    if (p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                        // Equipment stays on the battlefield unattached when the equipped creature leaves
                        p.setAttachedTo(null);
                        gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                        String logEntry = p.getCard().getName() + " becomes unattached (equipped creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(p.getCard(), " becomes unattached (equipped creature left the battlefield)."));
                        log.info("Game {} - {} unattached (equipped creature left)", gameData.id, p.getCard().getName());
                    } else {
                        it.remove();
                        gameData.expireFloatingEffectsForDepartedSource(p.getId());
                        boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, playerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(p.getCard(), " is put into the graveyard (enchanted creature left the battlefield)."));
                        log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                        if (wentToGraveyard) {
                            removals.add(new OrphanedAuraRemoval(p.getCard(), playerId));
                        }
                    }
                }
            }
        }
        creatureControlService.reconcileControl(gameData);
        return removals;
    }

    /**
     * Outcome of one {@link #enforceAttachmentLegality} sweep: auras put into the graveyard
     * (for the caller to fire graveyard triggers) and whether any equipment became unattached.
     */
    public record AttachmentLegalityResult(List<OrphanedAuraRemoval> removals, boolean anyUnattached) {
        public boolean anyChange() {
            return anyUnattached || !removals.isEmpty();
        }
    }

    /**
     * Enforces ongoing attachment legality for auras and equipment whose attached object is
     * still on the battlefield (the departed-object case is {@link #removeOrphanedAuras}):
     * an aura attached to an object it can't legally enchant — the object has protection from
     * it, or no longer satisfies the card's enchant restriction (its declared target filter) —
     * is put into its owner's graveyard (CR 704.5n); equipment attached to a permanent that is
     * not a creature or has protection from it becomes unattached (CR 704.5q).
     */
    public AttachmentLegalityResult enforceAttachmentLegality(GameData gameData) {
        List<OrphanedAuraRemoval> removals = new ArrayList<>();
        boolean anyUnattached = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (!p.isAttached()) continue;
                boolean isAura = p.getCard().getSubtypes().contains(CardSubtype.AURA);
                boolean isEquipment = p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
                if (!isAura && !isEquipment) continue;

                String reason = illegalAttachmentReason(gameData, p, playerId, isAura);
                if (reason == null) continue;

                if (isEquipment) {
                    // CR 704.5q — illegally attached equipment becomes unattached but stays
                    p.setAttachedTo(null);
                    gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                    anyUnattached = true;
                    String logEntry = p.getCard().getName() + " becomes unattached (" + reason + ").";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(p.getCard()).text(" becomes unattached (" + reason + ").").build());
                    log.info("Game {} - {} unattached ({})", gameData.id, p.getCard().getName(), reason);
                } else {
                    // CR 704.5n — an illegally attached aura is put into its owner's graveyard
                    it.remove();
                    gameData.expireFloatingEffectsForDepartedSource(p.getId());
                    boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, playerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                    String logEntry = p.getCard().getName() + " is put into the graveyard (" + reason + ").";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(p.getCard()).text(" is put into the graveyard (" + reason + ").").build());
                    log.info("Game {} - {} removed (illegally attached: {})", gameData.id, p.getCard().getName(), reason);
                    if (wentToGraveyard) {
                        removals.add(new OrphanedAuraRemoval(p.getCard(), playerId));
                    }
                }
            }
        }
        if (anyUnattached || !removals.isEmpty()) {
            creatureControlService.reconcileControl(gameData);
        }
        return new AttachmentLegalityResult(removals, anyUnattached);
    }

    /**
     * Why the attachment is illegal on its current host, or {@code null} while it is legal.
     * Hexproof and shroud are deliberately not checked — they restrict targeting, not staying
     * attached. Equipment ignores the equip ability's "creature you control" restriction:
     * attachment only requires the host to be a creature (CR 301.5c).
     */
    private String illegalAttachmentReason(GameData gameData, Permanent attachment, UUID controllerId, boolean isAura) {
        UUID attachedTo = attachment.getAttachedTo();
        if (gameData.playerIds.contains(attachedTo)) {
            // Aura enchanting a player (curse-style): illegal while the player has protection
            // from one of the aura's colors
            if (isAura) {
                for (CardColor color : gameQueryService.getEffectiveColors(gameData, attachment)) {
                    if (gameQueryService.playerHasProtectionFromColor(gameData, attachedTo, color)) {
                        return "enchanted player has protection from it";
                    }
                }
            }
            return null;
        }
        Permanent host = gameQueryService.findPermanentById(gameData, attachedTo);
        if (host == null) {
            // Departed host: removeOrphanedAuras owns that cleanup
            return null;
        }

        if (gameQueryService.hasProtectionFromSource(gameData, host, attachment)) {
            return (isAura ? "enchanted" : "equipped") + " permanent has protection from it";
        }

        if (!isAura) {
            if (!gameQueryService.isCreature(gameData, host)) {
                return "equipped permanent is no longer a creature";
            }
            return null;
        }

        // The card's declared target filter is its enchant restriction (e.g. "Enchant creature");
        // player-shaped filters never apply to a permanent host.
        TargetFilter filter = attachment.getCard().getTargetFilter();
        if (filter instanceof PermanentPredicateTargetFilter
                || filter instanceof ControlledPermanentPredicateTargetFilter
                || filter instanceof OwnedPermanentPredicateTargetFilter) {
            FilterContext context = new FilterContext(gameData, attachment.getCard().getId(), controllerId, null);
            if (predicateEvaluationService.checkTargetFilter(filter, host, context).isPresent()) {
                return "it can no longer enchant " + host.getCard().getName();
            }
        }
        return null;
    }
}

