package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Nicol Bolas, God-Pharaoh +1: each opponent exiles N cards from their hand (their choice).
 * APNAP order; opponents with empty hands are skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachOpponentExilesFromHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentExilesFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentExilesFromHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID activePlayerId = gameData.activePlayerId;

        // APNAP: active player first (if an opponent), then others in turn order.
        List<UUID> opponents = new ArrayList<>();
        if (!activePlayerId.equals(controllerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId) || playerId.equals(activePlayerId)) {
                continue;
            }
            opponents.add(playerId);
        }

        List<UUID> choosers = new ArrayList<>();
        for (UUID opponentId : opponents) {
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.isEmpty()) {
                String name = gameData.playerIdToName.get(opponentId);
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.text(name + " has no cards to exile from hand."));
                continue;
            }
            choosers.add(opponentId);
        }

        if (choosers.isEmpty()) {
            return;
        }

        UUID first = choosers.getFirst();
        List<UUID> remaining = choosers.size() > 1 ? List.copyOf(choosers.subList(1, choosers.size())) : List.of();
        playerInputService.beginExileFromHandChoice(gameData, first, entry.getSourcePermanentId(),
                null, e.amount(), remaining, e.amount());
    }
}
