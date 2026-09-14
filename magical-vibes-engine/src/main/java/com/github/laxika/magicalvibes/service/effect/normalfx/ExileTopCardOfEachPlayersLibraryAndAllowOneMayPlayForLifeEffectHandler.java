package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryAndAllowOneMayPlayForLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Nashi, Moon Sage's Scion's combat-damage trigger. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachPlayersLibraryAndAllowOneMayPlayForLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachPlayersLibraryAndAllowOneMayPlayForLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID grantId = UUID.randomUUID();
        for (UUID playerId : gameData.orderedPlayerIds) {
            var library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card card = library.removeFirst();
            exileService.exileCard(gameData, playerId, card, sourcePermanentId);
            gameData.exileCastPermissionsUntilEndOfTurn.add(new GameData.ExileCastPermission(
                    grantId, sourcePermanentId, controllerId, card.getId(), false, false, true));
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " exiles ")
                    .card(card)
                    .text(" from the top of their library (")
                    .card(entry.getCard())
                    .text(").")
                    .build());
        }
    }
}
