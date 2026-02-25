package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
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
        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameHelper.addCardToGraveyard(gameData, graveyardOwnerId, target.getOriginalCard(), Zone.BATTLEFIELD);
                gameData.stolenCreatures.remove(target.getId());
                gameHelper.collectDeathTrigger(gameData, target.getCard(), playerId, wasCreature);
                if (wasCreature) {
                    gameData.creatureDeathCountThisTurn.merge(playerId, 1, Integer::sum);
                    gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                }
                return true;
            }
        }
        return false;
    }

    public boolean removePermanentToExile(GameData gameData, Permanent target) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.playerExiledCards.get(ownerId).add(target.getOriginalCard());
                gameData.stolenCreatures.remove(target.getId());
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

    int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage, String sourceName) {
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
}
