package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Black Widow, Super Spy's combat-damage trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null || !gameData.playerIds.contains(damagedPlayerId)) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        String damagedPlayerName = gameData.playerIdToName.get(damagedPlayerId);
        Card nonland = null;
        int exiledCount = 0;
        while (library != null && !library.isEmpty()) {
            Card top = library.removeFirst();
            exileService.exileCard(gameData, damagedPlayerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                nonland = top;
                break;
            }
        }

        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), entry.getControllerId(), List.of(effect),
                    "Put a +1/+1 counter on " + entry.getCard().getName() + "?",
                    nonland == null ? null : nonland.getId(), null, entry.getSourcePermanentId()));
        } else if (nonland != null) {
            UUID cardId = nonland.getId();
            gameData.exilePlayPermissions.put(cardId, entry.getControllerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(cardId);
            gameData.exilePlayAnyManaType.add(cardId);
        }
        gameLogService.append(gameData, GameLog.text(
                damagedPlayerName + " exiles " + exiledCount + " card(s) from the top of their library."));
    }
}
