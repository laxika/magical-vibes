package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Grenzo, Havoc Raiser's cast permission mode. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryMayCastThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryMayCastThisTurnEffect.class;
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
        exileService.exileCard(gameData, damagedPlayerId, topCard);

        boolean castable = !topCard.hasType(CardType.LAND);
        if (castable) {
            gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
            gameData.exilePlayAnyManaType.add(topCard.getId());
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(damagedPlayerName + " exiles ").card(topCard)
                .text(" from the top of their library. ")
                .text(gameData.playerIdToName.get(controllerId)
                        + (castable ? " may cast it this turn." : " cannot cast it."))
                .build());
        log.info("Game {} - {} exiles {} from {}'s library; {}{}",
                gameData.id, damagedPlayerName, topCard.getName(), damagedPlayerName,
                gameData.playerIdToName.get(controllerId),
                castable ? " may cast it this turn" : " cannot cast it");
    }
}
