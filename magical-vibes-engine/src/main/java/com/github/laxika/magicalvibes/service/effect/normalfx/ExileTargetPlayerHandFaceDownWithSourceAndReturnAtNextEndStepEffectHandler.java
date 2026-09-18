package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandFaceDownWithSourceAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Apple of Eden's face-down hand exile and delayed returns. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPlayerHandFaceDownWithSourceAndReturnAtNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerHandFaceDownWithSourceAndReturnAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (targetPlayerId == null || sourcePermanentId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> cardsToExile = new ArrayList<>(hand);
        hand.clear();
        for (Card card : cardsToExile) {
            exileService.exileCardFaceDown(gameData, targetPlayerId, card, sourcePermanentId);
            gameData.queueDelayedAction(new ReturnExiledCardToHandAtNextEndStep(
                    card.getId(), targetPlayerId, entry.getCard(), entry.getControllerId()));
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + cardsToExile.size()
                + " card" + (cardsToExile.size() != 1 ? "s" : "")
                + " from their hand face down with " + entry.getCard().getName() + "."));
        log.info("Game {} - {} exiles {} hand cards face down with {} until the next end step",
                gameData.id, playerName, cardsToExile.size(), entry.getCard().getName());
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        if (entry.getSourcePermanentSnapshot() != null) {
            return entry.getSourcePermanentSnapshot().getId();
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return null;
    }
}
