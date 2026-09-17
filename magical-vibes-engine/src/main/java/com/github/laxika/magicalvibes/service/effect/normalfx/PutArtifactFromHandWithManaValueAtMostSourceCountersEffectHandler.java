package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutArtifactFromHandWithManaValueAtMostSourceCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutArtifactFromHandWithManaValueAtMostSourceCountersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutArtifactFromHandWithManaValueAtMostSourceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutArtifactFromHandWithManaValueAtMostSourceCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int maximumManaValue = source == null ? 0 : source.getCounterCount(e.counterType());

        UUID playerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (card.hasType(CardType.ARTIFACT) && card.getManaValue() <= maximumManaValue) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no artifact cards with mana value "
                    + maximumManaValue + " or less in hand."));
            log.info("Game {} - {} has no artifact cards with mana value {} or less in hand", gameData.id,
                    playerName, maximumManaValue);
            return;
        }

        playerInputService.beginCardChoice(gameData, playerId, validIndices,
                "You may put an artifact card with mana value " + maximumManaValue
                        + " or less from your hand onto the battlefield.");
    }
}
