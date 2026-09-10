package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Curse of Hospitality's combat-damage trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        UUID creatureControllerId = entry.getTriggeringPermanentControllerId();
        if (damagedPlayerId == null || creatureControllerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(damagedPlayerId);
        String damagedPlayerName = gameData.playerIdToName.get(damagedPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    damagedPlayerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, damagedPlayerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), creatureControllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
        if (!topCard.hasType(CardType.LAND)) {
            gameData.exilePlayAnyManaType.add(topCard.getId());
        }

        String creatureControllerName = gameData.playerIdToName.get(creatureControllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text(damagedPlayerName + " exiles ").card(topCard)
                .text(" from the top of their library — ")
                .text(creatureControllerName + " may play it this turn.")
                .build());
        log.info("Game {} - {} exiles {} from {}'s library top; {} may play it this turn",
                gameData.id, damagedPlayerName, topCard.getName(), damagedPlayerName,
                creatureControllerName);
    }
}
