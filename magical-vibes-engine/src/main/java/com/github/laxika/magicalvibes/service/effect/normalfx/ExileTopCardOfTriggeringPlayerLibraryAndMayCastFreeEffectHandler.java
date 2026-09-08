package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringPlayerId = entry.getTargetId();
        if (triggeringPlayerId == null || !gameData.playerIds.contains(triggeringPlayerId)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(triggeringPlayerId);
        String playerName = gameData.playerIdToName.get(triggeringPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, triggeringPlayerId, topCard);
        gameLogService.append(gameData,
                GameLog.builder().text(playerName + " exiles ").card(topCard)
                        .text(" from the top of their library.").build());

        if (topCard.hasType(CardType.LAND)) {
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + topCard.getName() + " without paying its mana cost?",
                topCard.getId()
        ));
        log.info("Game {} - {} may cast {} without paying its mana cost",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), topCard.getName());
    }
}
