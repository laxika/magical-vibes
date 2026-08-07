package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndDrawPerCreatureMilledEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillTargetPlayerAndDrawPerCreatureMilledEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final DrawService drawService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerAndDrawPerCreatureMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillTargetPlayerAndDrawPerCreatureMilledEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int cardsToMill = Math.min(Math.max(0, e.count()), deck == null ? 0 : deck.size());

        // Snapshot before milling so "put into their graveyard this way" can be counted afterwards,
        // excluding cards a replacement effect diverted somewhere other than the graveyard.
        List<Card> preview = cardsToMill == 0 ? List.of() : new ArrayList<>(deck.subList(0, cardsToMill));

        if (cardsToMill > 0) {
            graveyardService.resolveMillPlayer(gameData, targetPlayerId, e.count());
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        Set<Card> inGraveyard = graveyard == null ? Set.of() : new HashSet<>(graveyard);
        int creatureCount = 0;
        for (Card card : preview) {
            if (card.hasType(CardType.CREATURE) && inGraveyard.contains(card)) {
                creatureCount++;
            }
        }

        UUID controllerId = entry.getControllerId();
        for (int i = 0; i < creatureCount; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }

        if (creatureCount > 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " draws " + creatureCount
                    + " card" + (creatureCount != 1 ? "s" : "") + "."));
        }
        log.info("Game {} - {} milled {} card(s), {} creature card(s), drawing {}",
                gameData.id, entry.getCard().getName(), cardsToMill, creatureCount, creatureCount);
    }
}
