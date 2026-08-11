package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryGainLifeAndMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Daxos of Meletis's combat-damage trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryGainLifeAndMayCastThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryGainLifeAndMayCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(damagedPlayerId);
        String damagedPlayerName = gameData.playerIdToName.get(damagedPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(damagedPlayerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        int manaValue = topCard.getManaValue();
        exileService.exileCard(gameData, damagedPlayerId, topCard);

        if (manaValue > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, manaValue, entry.getCard().getName(),
                    entry.getCard(), entry.getEntryType());
        }

        // Daxos grants a cast permission, not a land-play permission.
        boolean castable = !topCard.hasType(CardType.LAND);
        if (castable) {
            gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
            gameData.exilePlayAnyManaType.add(topCard.getId());
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(damagedPlayerName + " exiles ").card(topCard)
                .text(" from the top of their library. ")
                .text(gameData.playerIdToName.get(controllerId) + " gains " + manaValue
                        + " life" + (castable ? " and may cast it this turn." : "."))
                .build());
        log.info("Game {} - {} exiles {} from {}'s library; {} gains {} life{}",
                gameData.id, damagedPlayerName, topCard.getName(), damagedPlayerName,
                gameData.playerIdToName.get(controllerId), manaValue,
                castable ? " and may cast it this turn" : "");
    }
}
