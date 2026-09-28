package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DiscardCardsAtNextTurnEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Kardum's death trigger and remembers the returned cards for its delayed discard. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (sourcePermanentId == null) {
            return;
        }

        UUID sourceId = sourcePermanentId;
        UUID controllerId = entry.getControllerId();
        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(exiled -> sourceId.equals(exiled.sourcePermanentId()))
                .filter(exiled -> controllerId.equals(exiled.ownerId()))
                .toList();
        List<UUID> returnedCardIds = toReturn.stream().map(exiled -> exiled.card().getId()).toList();

        for (ExiledCardEntry exiled : toReturn) {
            Card card = exiled.card();
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }
            gameData.addCardToHand(controllerId, card);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " puts ", card,
                    " from exile into their hand."));
        }

        if (!returnedCardIds.isEmpty()) {
            gameData.queueDelayedAction(new DiscardCardsAtNextTurnEndStep(
                    controllerId, returnedCardIds, entry.getCard(), controllerId, gameData.turnNumber));
        }
        log.info("Game {} - {} returns {} card(s) from exile and schedules their next-turn discard",
                gameData.id, entry.getCard().getName(), returnedCardIds.size());
    }
}
