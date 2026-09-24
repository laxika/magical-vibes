package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedStillExiledCardsEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect;
import com.github.laxika.magicalvibes.model.effect.PutStillExiledCardsIntoGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<UUID> exiledCardIds = new ArrayList<>();
        List<String> exiledNames = new ArrayList<>();
        for (int i = 0; i < exileEffect.count() && !library.isEmpty(); i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            if (gameData.findExiledCard(card.getId()) == null) {
                continue;
            }

            gameData.clearExilePlayPermissionGroup(card.getId());
            gameData.exilePlayPermissions.put(card.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            exiledCardIds.add(card.getId());
            exiledNames.add(card.getName());
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                    .card(card)
                    .text(" from the top of their library (may play it this turn).")
                    .build());
        }

        if (exiledCardIds.isEmpty()) {
            return;
        }

        gameData.queueDelayedAction(new DelayedStillExiledCardsEndStepTrigger(
                controllerId,
                entry.getCard(),
                entry.getSourcePermanentId(),
                new PutStillExiledCardsIntoGraveyardAndCreateTokensEffect(
                        exiledCardIds, exileEffect.tokenEffect()),
                exiledCardIds));
        log.info("Game {} - {} exiled {} cards for Glimpse's delayed token effect",
                gameData.id, gameData.playerIdToName.get(controllerId), exiledNames.size());
    }
}
