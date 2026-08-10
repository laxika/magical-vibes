package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TimesifterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimesifterEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TimesifterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> contenders = new ArrayList<>(gameData.orderedPlayerIds);
        while (!contenders.isEmpty()) {
            int greatestManaValue = -1;
            List<UUID> tiedPlayers = new ArrayList<>();

            for (UUID playerId : contenders) {
                List<Card> library = gameData.playerDecks.get(playerId);
                if (library == null || library.isEmpty()) {
                    log.info("Game {} - {} cannot exile a card for Timesifter",
                            gameData.id, gameData.playerIdToName.get(playerId));
                    continue;
                }

                Card topCard = library.removeFirst();
                exileService.exileCard(gameData, playerId, topCard);
                int manaValue = topCard.getManaValue();
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(playerId) + " exiles ")
                        .card(topCard)
                        .text(" from the top of their library for Timesifter (mana value "
                                + manaValue + ").")
                        .build());

                if (manaValue > greatestManaValue) {
                    greatestManaValue = manaValue;
                    tiedPlayers.clear();
                    tiedPlayers.add(playerId);
                } else if (manaValue == greatestManaValue) {
                    tiedPlayers.add(playerId);
                }
            }

            if (tiedPlayers.size() == 1) {
                UUID winnerId = tiedPlayers.getFirst();
                gameData.extraTurns.addFirst(winnerId);
                gameData.extraTurnSkipsUntap.addFirst(false);
                String winnerName = gameData.playerIdToName.get(winnerId);
                gameLogService.append(gameData,
                        GameLog.text(winnerName + " takes an extra turn after this one (Timesifter)."));
                log.info("Game {} - {} won the Timesifter comparison", gameData.id, winnerName);
                return;
            }

            contenders = tiedPlayers;
        }
    }
}
