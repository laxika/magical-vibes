package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PsychicBattleEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class PsychicBattleEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PsychicBattleSupport psychicBattleSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PsychicBattleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTriggeringCardId();
        if (targetCardId == null) {
            return;
        }

        int highestManaValue = -1;
        List<UUID> winners = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
            if (library.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(playerId) + " reveals no card from their library."));
                continue;
            }

            Card topCard = library.getFirst();
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " reveals ", topCard, " from the top of their library."));
            int manaValue = topCard.getManaValue();
            if (manaValue > highestManaValue) {
                highestManaValue = manaValue;
                winners.clear();
                winners.add(playerId);
            } else if (manaValue == highestManaValue) {
                winners.add(playerId);
            }
        }

        if (winners.size() != 1) {
            return;
        }

        StackEntry targetEntry = psychicBattleSupport.findTargetEntry(gameData, targetCardId);
        if (targetEntry == null || targetEntry.isNonTargeting()
                || psychicBattleSupport.targetIds(targetEntry).isEmpty()) {
            return;
        }

        if (psychicBattleSupport.queueNextChoice(gameData, entry.getCard(), winners.getFirst(), targetCardId, 0)) {
            playerInputService.processNextMayAbility(gameData);
        }
        log.info("Game {} - Psychic Battle winner is {}", gameData.id,
                gameData.playerIdToName.get(winners.getFirst()));
    }
}
