package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a damaged player's top-card play-permission trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var permission =
                (ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect)
                        effect;
        UUID damagedPlayerId = entry.getTargetId();
        UUID creatureControllerId = entry.getTriggeringPermanentId() == null ? null
                : gameQueryService.findPermanentController(gameData, entry.getTriggeringPermanentId());
        if (creatureControllerId == null) {
            creatureControllerId = entry.getTriggeringPermanentControllerId() != null
                    ? entry.getTriggeringPermanentControllerId() : entry.getControllerId();
        }
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

        int toExile = Math.min(permission.count(), deck.size());
        String creatureControllerName = gameData.playerIdToName.get(creatureControllerId);
        for (int i = 0; i < toExile; i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, damagedPlayerId, topCard);
            gameData.grantExilePlayPermission(topCard.getId(), creatureControllerId, Duration.UNTIL_END_OF_TURN);
            if (permission.withoutPayingManaCost()) {
                gameData.setExilePlayWithoutPayingManaCost(topCard.getId(), true);
            } else if (!topCard.hasType(CardType.LAND)) {
                gameData.grantExileAnyManaType(topCard.getId(), Duration.UNTIL_END_OF_TURN);
            }

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
}
