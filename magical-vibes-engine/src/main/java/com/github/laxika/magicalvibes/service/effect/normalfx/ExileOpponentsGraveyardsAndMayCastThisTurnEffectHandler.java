package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Mnemonic Betrayal's graveyard exile, cast permission, and delayed cleanup. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileOpponentsGraveyardsAndMayCastThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOpponentsGraveyardsAndMayCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int totalExiled = 0;

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }

            List<Card> graveyard = gameData.playerGraveyards.get(opponentId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            List<Card> exiledCards = new ArrayList<>(graveyard);
            graveyard.clear();
            graveyardService.notifyCardsExiledFromGraveyard(gameData, opponentId, exiledCards);

            for (Card card : exiledCards) {
                exileService.exileCard(gameData, opponentId, card);
                if (!card.hasType(CardType.LAND)) {
                    gameData.exilePlayPermissions.put(card.getId(), controllerId);
                    gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
                    gameData.exilePlayAnyManaType.add(card.getId());
                }
                gameData.queueDelayedAction(new ExileToOwnerGraveyardAtNextEndStep(
                        card.getId(), opponentId, entry.getCard()));
            }

            totalExiled += exiledCards.size();
            String playerName = gameData.playerIdToName.get(opponentId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s graveyard is exiled (" + exiledCards.size()
                            + " card" + (exiledCards.size() != 1 ? "s" : "") + ")."));
        }

        if (totalExiled == 0) {
            gameLogService.append(gameData, GameLog.text("Opponents' graveyards are already empty."));
        }
        log.info("Game {} - {} exiled {} cards from opponents' graveyards; nonland cards may be cast this turn",
                gameData.id, entry.getCard().getName(), totalExiled);
    }
}
