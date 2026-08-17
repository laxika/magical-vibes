package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Gonti, Night Minister's combat-damage trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect.class;
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
        exileService.exileCardFaceDown(gameData, damagedPlayerId, topCard,
                entry.getSourcePermanentId(), creatureControllerId);
        gameData.exilePlayPermissions.put(topCard.getId(), creatureControllerId);
        gameData.exilePlayAnyManaTypeWhileExiled.add(topCard.getId());

        String creatureControllerName = gameData.playerIdToName.get(creatureControllerId);
        gameLogService.append(gameData, GameLog.text(
                creatureControllerName + " looks at and exiles a card from " + damagedPlayerName
                        + "'s library face down with " + entry.getCard().getName() + "."));
        log.info("Game {} - {} looks at and exiles a card from {}'s library face down with {}",
                gameData.id, creatureControllerName, damagedPlayerName, entry.getCard().getName());
    }
}
