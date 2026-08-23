package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveTimeCounterFromExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveTimeCounterFromExiledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = ((RemoveTimeCounterFromExiledCardEffect) effect).cardId();
        removeTimeCounter(gameData, cardId);
    }

    public void removeTimeCounter(GameData gameData, UUID cardId) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        Integer counters = gameData.exiledCardTimeCounters.get(cardId);
        if (exiledEntry == null || counters == null || counters <= 0) {
            gameData.exiledCardTimeCounters.remove(cardId);
            return;
        }

        if (counters > 1) {
            int remaining = counters - 1;
            gameData.exiledCardTimeCounters.put(cardId, remaining);
            gameLogService.append(gameData,
                    GameLog.cardThen(exiledEntry.card(), " has a time counter removed (" + remaining + " remaining)."));
            return;
        }

        gameData.exiledCardTimeCounters.remove(cardId);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                exiledEntry.card(),
                exiledEntry.ownerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect(false, true)),
                "Cast " + exiledEntry.card().getName() + " without paying its mana cost?",
                cardId));
        gameLogService.append(gameData,
                GameLog.cardThen(exiledEntry.card(), " has no time counters left; its suspend ability may be cast."));
        log.info("Game {} - {} has no time counters left and may be cast from suspend",
                gameData.id, exiledEntry.card().getName());
    }
}
