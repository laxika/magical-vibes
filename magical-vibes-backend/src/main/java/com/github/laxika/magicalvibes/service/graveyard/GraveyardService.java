package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GraveyardService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    // @Lazy to break indirect circular dependency:
    // GraveyardService → TriggerCollectionService → PermanentRemovalService → GraveyardService
    private TriggerCollectionService triggerCollectionService;

    public GraveyardService(GameQueryService gameQueryService,
                            GameBroadcastService gameBroadcastService,
                            @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * Sets the TriggerCollectionService for manual (non-Spring) construction where
     * the circular dependency prevents passing it in the constructor.
     */
    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    // ===== Graveyard zone transitions =====

    public void resolveMillPlayer(GameData gameData, UUID targetPlayerId, int count) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int cardsToMill = Math.min(count, deck.size());
        List<Card> milledCards = new ArrayList<>(deck.subList(0, cardsToMill));
        deck.subList(0, cardsToMill).clear();
        for (Card card : milledCards) {
            addCardToGraveyard(gameData, targetPlayerId, card);
        }
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);
    }

    /**
     * Adds a card to its owner's graveyard, or applies a replacement effect (e.g. shuffle into library).
     * Returns true if the card was actually put into the graveyard, false if a replacement effect was applied.
     * Callers should skip "dies" / graveyard triggers when this returns false (CR 614.6).
     */
    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card) {
        return addCardToGraveyard(gameData, ownerId, card, null);
    }

    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        if (card.isShufflesIntoLibraryFromGraveyard()) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            Collections.shuffle(deck);
            String shuffleLog = card.getName() + " is revealed and shuffled into its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        } else {
            gameData.playerGraveyards.get(ownerId).add(card);
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, sourceZone);
            return true;
        }
    }

    // ===== Regeneration =====

    public boolean tryRegenerate(GameData gameData, Permanent perm) {
        if (perm.isCantRegenerateThisTurn()) {
            return false;
        }
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            perm.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
            perm.setAttacking(false);
            perm.setBlocking(false);
            perm.getBlockingTargets().clear();

            String logEntry = perm.getCard().getName() + " regenerates.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
            return true;
        }
        return false;
    }

    // ===== Damage tracking =====

    public void recordCreatureDamagedByPermanent(GameData gameData, UUID sourcePermanentId, Permanent damagedCreature, int damage) {
        if (sourcePermanentId == null || damagedCreature == null || damage <= 0) {
            return;
        }
        if (!gameQueryService.isCreature(gameData, damagedCreature)) {
            return;
        }

        gameData.creatureCardsDamagedThisTurnBySourcePermanent
                .computeIfAbsent(sourcePermanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(damagedCreature.getCard().getId());
    }

    // ===== Private helpers =====

    private void updateThisTurnBattlefieldToGraveyardTracking(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet());
        if (sourceZone == Zone.BATTLEFIELD
                && !card.isToken()
                && (card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE))) {
            tracked.add(card.getId());
            triggerDamagedCreatureDiesAbilities(gameData, card.getId());
        } else {
            tracked.remove(card.getId());
        }
    }

    private void triggerDamagedCreatureDiesAbilities(GameData gameData, UUID dyingCreatureCardId) {
        if (dyingCreatureCardId == null) {
            return;
        }

        for (Map.Entry<UUID, Set<UUID>> entry : gameData.creatureCardsDamagedThisTurnBySourcePermanent.entrySet()) {
            UUID sourcePermanentId = entry.getKey();
            Set<UUID> damagedCreatureIds = entry.getValue();
            if (!damagedCreatureIds.contains(dyingCreatureCardId)) {
                continue;
            }

            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (source == null) {
                continue;
            }

            UUID controllerId = findPermanentController(gameData, sourcePermanentId);
            if (controllerId == null) {
                continue;
            }

            List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        sourcePermanentId
                ));
                String triggerLog = source.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (damaged creature died this turn)", gameData.id, source.getCard().getName());
            }
        }

        for (Set<UUID> damagedCreatureIds : gameData.creatureCardsDamagedThisTurnBySourcePermanent.values()) {
            damagedCreatureIds.remove(dyingCreatureCardId);
        }
    }

    private UUID findPermanentController(GameData gameData, UUID permanentId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(permanentId)) {
                    return playerId;
                }
            }
        }
        return null;
    }
}
