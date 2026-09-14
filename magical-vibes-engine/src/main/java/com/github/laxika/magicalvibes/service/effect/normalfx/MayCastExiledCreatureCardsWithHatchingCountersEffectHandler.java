package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCreatureCardsWithHatchingCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastExiledCreatureCardsWithHatchingCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastExiledCreatureCardsWithHatchingCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<ExiledCardEntry> eligible = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                Card card = exiled.card();
                if (controllerId.equals(exiled.ownerId())
                        && gameData.exiledCardsWithHatchingCounters.contains(card.getId())
                        && card.hasType(CardType.CREATURE)) {
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
                    "Cast " + card.getName() + " without paying its mana cost?",
                    card.getId()
            ));
        }

        if (eligible.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " has no exiled creature cards with hatching counters to cast."));
        } else {
            log.info("Game {} - {} offers a free cast of {} hatching-counter creature(s)",
                    gameData.id, entry.getCard().getName(), eligible.size());
        }
    }
}
