package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermanentRemovalService {

    private final GameHelper gameHelper;
    private final AuraAttachmentService auraAttachmentService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    public boolean removePermanentToGraveyard(GameData gameData, Permanent target) {
        // Replacement effect: exile instead of going to graveyard (CR 614.6)
        if (target.isExileIfLeavesBattlefield()) {
            boolean exiled = removePermanentToExile(gameData, target);
            if (exiled) {
                String logEntry = target.getCard().getName() + " is exiled instead of going to the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                removeOrphanedAuras(gameData);
            }
            return exiled;
        }

        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        boolean wasArtifact = gameQueryService.isArtifact(target);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                boolean wentToGraveyard = gameHelper.addCardToGraveyard(gameData, graveyardOwnerId, target.getOriginalCard(), Zone.BATTLEFIELD);
                gameData.stolenCreatures.remove(target.getId());
                // Skip "dies" and graveyard triggers if a replacement effect redirected the card (CR 614.6)
                if (wentToGraveyard) {
                    gameHelper.collectDeathTrigger(gameData, target.getCard(), playerId, wasCreature);
                    if (wasCreature) {
                        gameData.creatureDeathCountThisTurn.merge(playerId, 1, Integer::sum);
                        gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                        gameHelper.checkAnyNontokenCreatureDeathTriggers(gameData, target.getCard());
                        gameHelper.checkOpponentCreatureDeathTriggers(gameData, playerId);
                        gameHelper.checkEquippedCreatureDeathTriggers(gameData, target.getId(), playerId);
                    }
                    if (wasArtifact) {
                        gameHelper.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gameData, graveyardOwnerId, playerId);
                    }
                }
                handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
                handleExileReturnOnLeave(gameData, target);
                return true;
            }
        }
        return false;
    }

    public boolean removePermanentToHand(GameData gameData, Permanent target) {
        // Replacement effect: exile instead of going to hand (CR 614.6)
        if (target.isExileIfLeavesBattlefield()) {
            boolean exiled = removePermanentToExile(gameData, target);
            if (exiled) {
                String logEntry = target.getCard().getName() + " is exiled instead of returning to hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                removeOrphanedAuras(gameData);
            }
            return exiled;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.stolenCreatures.remove(target.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(target.getOriginalCard());
                handleExileReturnOnLeave(gameData, target);
                return true;
            }
        }
        return false;
    }

    public boolean removePermanentToExile(GameData gameData, Permanent target) {
        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.playerExiledCards.get(ownerId).add(target.getOriginalCard());
                gameData.stolenCreatures.remove(target.getId());
                handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
                handleExileReturnOnLeave(gameData, target);
                return true;
            }
        }
        return false;
    }

    public void removeOrphanedAuras(GameData gameData) {
        auraAttachmentService.removeOrphanedAuras(gameData);
    }

    /**
     * Attempts to destroy a permanent, handling indestructible and regeneration checks.
     * If destroyed, also removes orphaned auras. Returns true if the permanent was destroyed.
     */
    public boolean tryDestroyPermanent(GameData gameData, Permanent target) {
        return tryDestroyPermanent(gameData, target, false);
    }

    public boolean tryDestroyPermanent(GameData gameData, Permanent target, boolean cannotBeRegenerated) {
        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            String logEntry = target.getCard().getName() + " is indestructible.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is indestructible, destroy prevented", gameData.id, target.getCard().getName());
            return false;
        }
        if (!cannotBeRegenerated && gameHelper.tryRegenerate(gameData, target)) {
            return false;
        }
        removePermanentToGraveyard(gameData, target);
        removeOrphanedAuras(gameData);
        return true;
    }

    public void removeCardFromGraveyardById(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            if (graveyard.removeIf(c -> c.getId().equals(cardId))) {
                Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(playerId);
                if (tracked != null) {
                    tracked.remove(cardId);
                }
                return;
            }
        }
    }

    public int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage, String sourceName) {
        if (damage <= 0) return damage;
        Permanent target = gameQueryService.findEnchantedCreatureByAuraEffect(gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class);
        if (target == null) return damage;

        int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, target, damage);
        String logEntry = target.getCard().getName() + " absorbs " + effectiveDamage + " redirected " + sourceName + " damage.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                String indestructibleLog = target.getCard().getName() + " is indestructible and survives.";
                gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
            } else {
                removePermanentToGraveyard(gameData, target);
                String deathLog = target.getCard().getName() + " is destroyed by redirected " + sourceName + " damage.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
                removeOrphanedAuras(gameData);
            }
        }

        return 0;
    }

    /**
     * Returns the ID of the creature that should be sacrificed if the given permanent is an equipment
     * with SacrificeOnUnattachEffect that is currently attached to a creature. Returns null otherwise.
     */
    private UUID getSacrificeOnUnattachCreatureId(Permanent equipment) {
        if (equipment.getAttachedTo() == null) return null;
        if (!equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return null;
        boolean hasEffect = equipment.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);
        return hasEffect ? equipment.getAttachedTo() : null;
    }

    /**
     * After an equipment with SacrificeOnUnattachEffect is removed from the battlefield,
     * sacrifice the creature it was attached to (if it still exists).
     */
    private void handleSacrificeOnUnattach(GameData gameData, Permanent removedEquipment, UUID creatureId) {
        if (creatureId == null) return;
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null) return;
        String sacrificeLog = creature.getCard().getName() + " is sacrificed (" + removedEquipment.getCard().getName() + " became unattached).";
        gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
        log.info("Game {} - {} sacrificed due to {} leaving battlefield", gameData.id, creature.getCard().getName(), removedEquipment.getCard().getName());
        removePermanentToGraveyard(gameData, creature);
        removeOrphanedAuras(gameData);
    }

    /**
     * Checks if the removed permanent had an exile-until-source-leaves tracking entry.
     * If so, returns the exiled card to the battlefield under its owner's control.
     */
    private void handleExileReturnOnLeave(GameData gameData, Permanent removedPermanent) {
        PendingExileReturn pending = gameData.exileReturnOnPermanentLeave.remove(removedPermanent.getId());
        if (pending == null) return;

        Card exiledCard = pending.card();
        UUID ownerId = pending.controllerId();

        // Remove card from exile zone
        List<Card> exiledCards = gameData.playerExiledCards.get(ownerId);
        if (exiledCards != null && exiledCards.remove(exiledCard)) {
            // Return as a new permanent
            Permanent perm = new Permanent(exiledCard);
            gameHelper.putPermanentOntoBattlefield(gameData, ownerId, perm);
            String playerName = gameData.playerIdToName.get(ownerId);
            String logEntry = exiledCard.getName() + " returns to the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returns from exile (source left battlefield)", gameData.id, exiledCard.getName());
            gameHelper.handleCreatureEnteredBattlefield(gameData, ownerId, exiledCard, null, false);
        } else {
            log.info("Game {} - Exiled card {} no longer in exile zone, return skipped", gameData.id, exiledCard.getName());
        }
    }
}
