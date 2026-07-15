package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveEggCounterFromExileAndReturnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveEggCounterFromExileAndReturnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveEggCounterFromExileAndReturnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveEggCounterFromExileAndReturnEffect) effect;
        UUID cardId = e.cardId();
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} egg counter removed, {} remaining", gameData.id, exiledCard.getName(), remaining);
        } else {
            // Last counter removed — return to the battlefield
            gameData.exiledCardEggCounters.remove(cardId);

            // Remove card from exile zone
            UUID ownerId = gameQueryService.findExileOwnerById(gameData, cardId);
            gameData.removeFromExile(cardId);

            // Return to the battlefield under its owner's control
            UUID returnControllerId = ownerId != null ? ownerId : controllerId;
            Permanent perm = new Permanent(exiledCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, returnControllerId, perm);

            String logEntry = exiledCard.getName() + " has its last egg counter removed and returns to the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} returns to the battlefield from exile (all egg counters removed)",
                    gameData.id, exiledCard.getName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, returnControllerId, exiledCard, null, false);
        }
    }
}
