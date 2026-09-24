package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseVoidCounterCardMayPlayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Queues mutually exclusive free-play offers for Dauthi Voidwalker's void-counter cards. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseVoidCounterCardMayPlayEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseVoidCounterCardMayPlayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<ExiledCardEntry> eligible = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (!gameData.exiledCardsWithVoidCounters.contains(exiled.card().getId())
                        || controllerId.equals(exiled.ownerId())) {
                    continue;
                }
                eligible.add(exiled);
            }
        }

        for (int i = eligible.size() - 1; i >= 0; i--) {
            ExiledCardEntry exiled = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    exiled.card(),
                    controllerId,
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect(true)),
                    "Play " + exiled.card().getName() + " without paying its mana cost?",
                    exiled.card().getId()));
        }

        if (eligible.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " has no opponent-owned exiled cards with void counters to play."));
        } else {
            log.info("Game {} - {} offers {} void-counter exiled card(s) for free play",
                    gameData.id, entry.getCard().getName(), eligible.size());
        }
    }
}
