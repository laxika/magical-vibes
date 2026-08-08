package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDrawByManaValueEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillControllerAndDrawByManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final DrawService drawService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndDrawByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty — no cards are milled or drawn."));
            return;
        }

        // Snapshot the top card before milling so its mana value is known afterwards.
        Card milled = deck.getFirst();
        graveyardService.resolveMillPlayer(gameData, controllerId, 1);

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || !graveyard.contains(milled)) {
            // A replacement effect diverted the card, so it was never milled.
            return;
        }

        int drawCount = milled.getManaValue();
        for (int i = 0; i < drawCount; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        gameLogService.append(gameData, GameLog.builder().text(playerName + " mills ").card(milled)
                .text(" and draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + ".").build());
        log.info("Game {} - {} milled {} (mana value {}), drawing {}", gameData.id, playerName, milled.getName(),
                drawCount, drawCount);
    }
}
