package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileControllerHandWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the controller's whole-hand source-linked exile effect. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileControllerHandWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileControllerHandWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> toExile = new ArrayList<>(hand);
        hand.clear();
        for (Card card : toExile) {
            exileService.exileCard(gameData, controllerId, card, sourcePermanentId);
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " exiles " + toExile.size()
                + " card" + (toExile.size() != 1 ? "s" : "") + " from their hand."));
        log.info("Game {} - {} exiles their hand ({} cards) with {}",
                gameData.id, controllerName, toExile.size(), entry.getCard().getName());
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
