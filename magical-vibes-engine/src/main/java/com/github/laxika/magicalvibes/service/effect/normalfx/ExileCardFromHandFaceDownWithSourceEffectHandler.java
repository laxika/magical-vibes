package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * The controller chooses a card in their hand and exiles it face down, tracked with the source
 * permanent, so only they may look at it while it remains exiled. Gustha's Scepter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileCardFromHandFaceDownWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromHandFaceDownWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCardFromHandFaceDownWithSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) {
            // The source has left the battlefield: nothing to track the exiled card with.
            return;
        }

        if (e.toGraveyardOnControlLoss()) {
            gameData.exiledCardsToGraveyardOnControlLossWatch.put(sourcePermanentId, controllerId);
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " has no cards to exile from hand."));
            return;
        }

        playerInputService.beginExileFromHandChoice(gameData, controllerId, sourcePermanentId,
                null, 1, List.of(), 0, true);
    }

    /**
     * An activated ability's stack entry carries no source permanent id, so fall back to the
     * battlefield permanent whose card is the ability's source card.
     */
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
