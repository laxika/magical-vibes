package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromLibraryMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves {@link ExileRandomCardFromLibraryMayPlayThisTurnEffect}: a random card of the controller's
 * library is exiled and may be played this turn. The library is not shuffled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomCardFromLibraryMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardFromLibraryMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileRandomCardFromLibraryMayPlayThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card exiled = deck.remove(ThreadLocalRandom.current().nextInt(deck.size()));
        exileService.exileCard(gameData, controllerId, exiled);
        gameData.exilePlayPermissions.put(exiled.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiled.getId());
        if (exileEffect.withoutPayingManaCost()) {
            gameData.exilePlayWithoutPayingManaCost.add(exiled.getId());
        }

        String playNote = exileEffect.withoutPayingManaCost()
                ? " (may play it without paying its mana cost this turn)"
                : " (may play it this turn)";
        gameLogService.append(gameData, GameLog.builder()
                .text(controllerName + " exiles ").card(exiled)
                .text(" at random from their library" + playNote + ".").build());
        log.info("Game {} - {} exiles {} at random from library (withoutPaying={})",
                gameData.id, controllerName, exiled.getName(), exileEffect.withoutPayingManaCost());

        triggerCollectionService.checkControllerCardsExiledFromLibraryOrGraveyardTriggers(
                gameData, controllerId, 1);
    }
}
