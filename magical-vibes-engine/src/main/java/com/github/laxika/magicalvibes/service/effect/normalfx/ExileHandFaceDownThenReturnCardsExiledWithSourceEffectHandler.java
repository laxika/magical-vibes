package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileHandFaceDownThenReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Duplicity's upkeep swap: exile the controller's whole hand face down, tracked with the source
 * permanent, then put every other card they own already exiled with that permanent into their hand.
 * The "other" pile is snapshotted before the hand is exiled so the two piles swap instead of merging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileHandFaceDownThenReturnCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileHandFaceDownThenReturnCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) return;

        String controllerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        List<Card> previouslyExiled = gameData.exiledCards.stream()
                .filter(e -> sourcePermanentId.equals(e.sourcePermanentId())
                        && controllerId.equals(e.ownerId()))
                .map(ExiledCardEntry::card)
                .toList();

        List<Card> hand = gameData.playerHands.get(controllerId);
        int exiledCount = 0;
        if (hand != null && !hand.isEmpty()) {
            List<Card> handCards = new ArrayList<>(hand);
            hand.clear();
            for (Card card : handCards) {
                exileService.exileCardFaceDown(gameData, controllerId, card, sourcePermanentId);
                exiledCount++;
            }
        }
        gameLogService.append(gameData, GameLog.text(controllerName + " exiles " + exiledCount
                + " card" + (exiledCount != 1 ? "s" : "") + " from their hand face down with " + sourceName + "."));

        for (Card card : previouslyExiled) {
            gameData.removeFromExile(card.getId());
            gameData.addCardToHand(controllerId, card);
        }
        if (!previouslyExiled.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " puts " + previouslyExiled.size()
                    + " card" + (previouslyExiled.size() != 1 ? "s" : "") + " exiled with " + sourceName
                    + " into their hand."));
        }

        log.info("Game {} - {} swaps {} hand cards for {} cards exiled with {}",
                gameData.id, controllerName, exiledCount, previouslyExiled.size(), sourceName);
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) return entry.getSourcePermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return null;
        for (Permanent p : battlefield) {
            if (p.getCard() == entry.getCard()) return p.getId();
        }
        return null;
    }
}
