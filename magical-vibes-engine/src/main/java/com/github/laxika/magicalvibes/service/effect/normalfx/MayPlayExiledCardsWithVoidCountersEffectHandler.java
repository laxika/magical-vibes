package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardsWithVoidCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Offers one opponent-owned exiled void-counter card at a time, with the offers mutually exclusive. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayPlayExiledCardsWithVoidCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPlayExiledCardsWithVoidCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<ExiledCardEntry> eligible = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                Card card = exiled.card();
                if (!controllerId.equals(exiled.ownerId())
                        && gameData.exiledCardsWithVoidCounters.contains(card.getId())) {
                    eligible.add(exiled);
                }
            }
        }

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i).card();
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect(true)),
                    "Play " + card.getName() + " without paying its mana cost?",
                    card.getId()
            ));
        }

        if (eligible.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " has no exiled cards with void counters to play."));
        } else {
            log.info("Game {} - {} offers a free play of {} void-counter card(s)",
                    gameData.id, entry.getCard().getName(), eligible.size());
        }
    }
}
