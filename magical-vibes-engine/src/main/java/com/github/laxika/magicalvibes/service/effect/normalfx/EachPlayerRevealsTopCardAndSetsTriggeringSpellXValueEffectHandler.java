package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardAndSetsTriggeringSpellXValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerRevealsTopCardAndSetsTriggeringSpellXValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardAndSetsTriggeringSpellXValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();
        int totalManaValue = 0;

        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> library = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            if (library == null || library.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                continue;
            }

            Card topCard = library.getFirst();
            int manaValue = topCard.getManaValue();
            totalManaValue += manaValue;

            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(topCard)
                    .text(" (mana value " + manaValue + ") from the top of their library ("
                            + sourceName + ").")
                    .build());
        }

        StackEntry triggeringSpell = gameQueryService.findStackEntryByCardId(
                gameData, entry.getTriggeringCardId());
        if (triggeringSpell != null) {
            triggeringSpell.setXValue(totalManaValue);
            log.info("Game {} - {} sets {}'s X value to {} from revealed top cards",
                    gameData.id, sourceName, triggeringSpell.getCard().getName(), totalManaValue);
        }
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        if (gameData.activePlayerId != null) {
            order.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!order.contains(playerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
