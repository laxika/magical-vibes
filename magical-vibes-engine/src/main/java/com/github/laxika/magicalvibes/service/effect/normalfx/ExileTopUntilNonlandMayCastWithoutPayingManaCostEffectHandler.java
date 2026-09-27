package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves an exile-until-nonland effect with an immediate free-cast choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandMayCastWithoutPayingManaCostEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandMayCastWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty — nothing is exiled."));
            return;
        }

        Card nonland = null;
        int exiledCount = 0;
        while (!library.isEmpty()) {
            Card top = library.removeFirst();
            exileService.exileCard(gameData, controllerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                nonland = top;
                break;
            }
        }

        if (nonland == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + exiledCount
                            + " card(s) from the top of their library — no nonland card found."));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + nonland.getName() + " without paying its mana cost?",
                nonland.getId()));
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles cards until ").card(nonland)
                .text(" and may cast it without paying its mana cost.")
                .build());
        log.info("Game {} - {} exiles {} card(s) until {} for a free cast",
                gameData.id, playerName, exiledCount, nonland.getName());
    }
}
