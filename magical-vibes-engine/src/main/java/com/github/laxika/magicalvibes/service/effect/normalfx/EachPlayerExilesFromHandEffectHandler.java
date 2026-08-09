package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an effect that has every player exile cards from their hand in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachPlayerExilesFromHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerExilesFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerExilesFromHandEffect) effect;
        UUID activePlayerId = gameData.activePlayerId;

        List<UUID> players = new ArrayList<>();
        if (activePlayerId != null && gameData.playerIds.contains(activePlayerId)) {
            players.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                players.add(playerId);
            }
        }

        List<UUID> choosers = new ArrayList<>();
        for (UUID playerId : players) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(gameData.playerIdToName.get(playerId) + " has no cards to exile from hand."));
                continue;
            }
            choosers.add(playerId);
        }

        if (choosers.isEmpty()) {
            return;
        }

        UUID first = choosers.getFirst();
        List<UUID> remaining = choosers.size() > 1
                ? List.copyOf(choosers.subList(1, choosers.size()))
                : List.of();
        playerInputService.beginExileFromHandChoice(gameData, first, entry.getSourcePermanentId(),
                null, e.amount(), remaining, e.amount());
    }
}
