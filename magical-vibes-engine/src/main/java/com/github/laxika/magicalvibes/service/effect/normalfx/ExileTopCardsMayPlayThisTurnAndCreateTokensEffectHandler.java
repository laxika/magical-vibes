package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsMayPlayThisTurnAndCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayPlayThisTurnAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsMayPlayThisTurnAndCreateTokensEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        int landCount = 0;
        int exiledCount = Math.min(e.count(), library.size());
        for (int i = 0; i < exiledCount; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            gameData.exilePlayPermissions.put(card.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            if (card.hasType(CardType.LAND)) {
                landCount++;
            }

            gameLogService.append(gameData, GameLog.builder()
                    .text(controllerName + " exiles ")
                    .card(card)
                    .text(" from the top of their library (may play it this turn).")
                    .build());
        }

        int nonlandCount = exiledCount - landCount;
        if (landCount > 0) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, controllerId, e.landToken(), 1, entry.getCard().getSetCode()));
        }
        if (nonlandCount > 0) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, controllerId, e.nonlandToken(), 1, entry.getCard().getSetCode()));
        }

        log.info("Game {} - {} exiled {} cards and found {} land and {} nonland cards",
                gameData.id, controllerName, exiledCount, landCount, nonlandCount);
    }
}
