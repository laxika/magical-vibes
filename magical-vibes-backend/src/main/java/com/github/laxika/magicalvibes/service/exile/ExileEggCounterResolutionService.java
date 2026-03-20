package com.github.laxika.magicalvibes.service.exile;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RemoveEggCounterFromExileAndReturnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the upkeep trigger for exiled cards with egg counters
 * (e.g. Darigaaz Reincarnated).
 *
 * <p>"Remove an egg counter from it. Then if this card has no egg counters on it,
 * return it to the battlefield."</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExileEggCounterResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;

    @HandlesEffect(RemoveEggCounterFromExileAndReturnEffect.class)
    void resolveRemoveEggCounterAndReturn(GameData gameData, StackEntry entry,
                                           RemoveEggCounterFromExileAndReturnEffect effect) {
        UUID cardId = effect.cardId();
        UUID controllerId = entry.getControllerId();

        // Intervening-if re-check at resolution: card must still be exiled with an egg counter
        Integer counters = gameData.exiledCardEggCounters.get(cardId);
        if (counters == null || counters <= 0) {
            log.info("Game {} - Egg counter ability fizzles (card no longer exiled with egg counters)", gameData.id);
            return;
        }

        Card exiledCard = gameQueryService.findCardInExileById(gameData, cardId);
        if (exiledCard == null) {
            gameData.exiledCardEggCounters.remove(cardId);
            log.info("Game {} - Egg counter ability fizzles (card no longer in exile)", gameData.id);
            return;
        }

        // Remove one egg counter
        int remaining = counters - 1;
        if (remaining > 0) {
            gameData.exiledCardEggCounters.put(cardId, remaining);
            String logEntry = exiledCard.getName() + " has an egg counter removed (" + remaining + " remaining).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} egg counter removed, {} remaining", gameData.id, exiledCard.getName(), remaining);
        } else {
            // Last counter removed — return to the battlefield
            gameData.exiledCardEggCounters.remove(cardId);

            // Remove card from exile zone
            UUID ownerId = gameQueryService.findExileOwnerById(gameData, cardId);
            if (ownerId != null) {
                List<Card> exile = gameData.playerExiledCards.get(ownerId);
                if (exile != null) {
                    exile.removeIf(c -> c.getId().equals(cardId));
                }
            }

            // Return to the battlefield under its owner's control
            UUID returnControllerId = ownerId != null ? ownerId : controllerId;
            Permanent perm = new Permanent(exiledCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, perm);

            String logEntry = exiledCard.getName() + " has its last egg counter removed and returns to the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returns to the battlefield from exile (all egg counters removed)",
                    gameData.id, exiledCard.getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, exiledCard, null, false);
        }
    }
}
