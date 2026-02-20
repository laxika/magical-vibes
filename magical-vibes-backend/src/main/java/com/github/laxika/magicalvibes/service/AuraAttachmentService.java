package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuraAttachmentService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    public void removeOrphanedAuras(GameData gameData) {
        boolean anyRemoved = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (p.getAttachedTo() != null && gameQueryService.findPermanentById(gameData, p.getAttachedTo()) == null) {
                    if (p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                        // Equipment stays on the battlefield unattached when the equipped creature leaves
                        p.setAttachedTo(null);
                        String logEntry = p.getCard().getName() + " becomes unattached (equipped creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} unattached (equipped creature left)", gameData.id, p.getCard().getName());
                    } else {
                        it.remove();
                        addCardToGraveyard(gameData, playerId, p.getOriginalCard());
                        String logEntry = p.getCard().getName() + " is put into the graveyard (enchanted creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                        anyRemoved = true;
                    }
                }
            }
        }
        if (anyRemoved) {

        }
        returnStolenCreatures(gameData, false);
    }

    public void returnStolenCreatures(GameData gameData, boolean includeUntilEndOfTurn) {
        if (gameData.stolenCreatures.isEmpty()) return;

        boolean anyReturned = false;
        Iterator<Map.Entry<UUID, UUID>> it = gameData.stolenCreatures.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            UUID creatureId = entry.getKey();
            UUID ownerId = entry.getValue();
            boolean isUntilEndOfTurnSteal = gameData.untilEndOfTurnStolenCreatures.contains(creatureId);

            if (includeUntilEndOfTurn && !isUntilEndOfTurnSteal) {
                continue;
            }
            if (!includeUntilEndOfTurn && isUntilEndOfTurnSteal) {
                continue;
            }

            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature == null) {
                it.remove();
                gameData.enchantmentDependentStolenCreatures.remove(creatureId);
                gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
                continue;
            }

            if (gameQueryService.hasAuraWithEffect(gameData, creature, ControlEnchantedCreatureEffect.class)) {
                if (includeUntilEndOfTurn) {
                    gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
                }
                continue;
            }

            if (gameData.enchantmentDependentStolenCreatures.contains(creatureId)
                    && gameQueryService.isEnchanted(gameData, creature)) {
                if (includeUntilEndOfTurn) {
                    gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
                }
                continue;
            }
            gameData.enchantmentDependentStolenCreatures.remove(creatureId);

            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf != null && bf.remove(creature)) {
                    gameData.playerBattlefields.get(ownerId).add(creature);
                    creature.setSummoningSick(true);

                    String ownerName = gameData.playerIdToName.get(ownerId);
                    String logEntry = creature.getCard().getName() + " returns to " + ownerName + "'s control.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns to {}'s control", gameData.id, creature.getCard().getName(), ownerName);
                    anyReturned = true;
                    break;
                }
            }
            it.remove();
            gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
        }
        if (anyReturned) {

        }
    }

    private void addCardToGraveyard(GameData gameData, UUID ownerId, Card card) {
        if (card.isShufflesIntoLibraryFromGraveyard()) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            Collections.shuffle(deck);
            String shuffleLog = card.getName() + " is revealed and shuffled into its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
        } else {
            gameData.playerGraveyards.get(ownerId).add(card);
        }
    }
}

