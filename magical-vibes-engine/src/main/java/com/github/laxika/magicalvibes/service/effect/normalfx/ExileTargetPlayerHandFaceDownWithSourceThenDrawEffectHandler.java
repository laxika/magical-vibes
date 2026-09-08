package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandFaceDownWithSourceThenDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the hand exile and equal draw for Induced Amnesia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPlayerHandFaceDownWithSourceThenDrawEffectHandler
        implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerHandFaceDownWithSourceThenDrawEffect.class;
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
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String cardName = entry.getCard().getName();
        int count = cardsToExile.size();
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + count
                + " card" + (count != 1 ? "s" : "") + " from their hand face down with " + cardName + "."));

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, targetPlayerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + " draws " + count
                + " card" + (count != 1 ? "s." : ".")));
        log.info("Game {} - {} exiles {} hand cards face down with {} and draws the same number",
                gameData.id, playerName, count, cardName);
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
