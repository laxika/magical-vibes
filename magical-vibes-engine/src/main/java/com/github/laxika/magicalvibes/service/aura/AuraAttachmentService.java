package com.github.laxika.magicalvibes.service.aura;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
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
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} unattached (equipped creature left)", gameData.id, p.getCard().getName());
                    } else {
                        it.remove();
                        gameData.expireFloatingEffectsForDepartedSource(p.getId());
                        boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, playerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                        String logEntry = p.getCard().getName() + " is put into the graveyard (enchanted creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
}

