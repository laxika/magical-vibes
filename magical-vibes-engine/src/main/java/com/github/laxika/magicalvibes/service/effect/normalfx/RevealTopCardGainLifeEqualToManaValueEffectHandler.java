package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardGainLifeEqualToManaValueEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardGainLifeEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        int manaValue = topCard.getManaValue();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" (mana value " + manaValue + ") from the top of their library (" + sourceName + ").")
                .build());

        if (manaValue > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, manaValue, sourceName,
                    entry.getCard(), entry.getEntryType());
        }

        log.info("Game {} - {} reveals {} (MV {}) via {} — card stays on top",
                gameData.id, playerName, topCard.getName(), manaValue, sourceName);
    }
}
