package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RestartTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestartTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnProgressionService turnProgressionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RestartTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<ExiledCardEntry> earlierRestartExiles = gameData.exiledCards.stream()
                .filter(exiled -> exiled.card().getEffects(EffectSlot.SPELL).stream()
                        .anyMatch(RestartTurnEffect.class::isInstance))
                .toList();
        boolean restored = gameData.restoreTurnStartSnapshot();
        gameData.restartTurnRequested = true;
        gameLogService.append(gameData, GameLog.text("The turn restarts."));

        if (restored) {
            // Restart cards stay exiled: the turn-start snapshot must not bring back the card being
            // resolved or any restart card exiled by an earlier resolution.
            Set<Card> exiledForGood = Collections.newSetFromMap(new IdentityHashMap<>());
            exiledForGood.add(entry.getCard());
            earlierRestartExiles.forEach(exiled -> exiledForGood.add(exiled.card()));
            gameData.playerHands.values().forEach(hand -> hand.removeIf(exiledForGood::contains));
            gameData.playerDecks.values().forEach(deck -> deck.removeIf(exiledForGood::contains));
            gameData.playerGraveyards.values().forEach(graveyard -> graveyard.removeIf(exiledForGood::contains));
            for (ExiledCardEntry exiled : earlierRestartExiles) {
                boolean stillExiled = gameData.exiledCards.stream()
                        .anyMatch(current -> current.card() == exiled.card());
                if (!stillExiled) {
                    gameData.exiledCards.add(exiled);
                }
            }
            turnProgressionService.restartTurnFromBeginning(gameData);
            log.info("Game {} - Restart the turn effect resolved", gameData.id);
        } else {
            log.warn("Game {} - Restart the turn effect resolved without a turn-start snapshot",
                    gameData.id);
        }
    }
}
