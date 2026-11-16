package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandFaceDownAndReturnAtNextTurnEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardsToTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the hand-exile half of Suppress and registers its delayed return. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPlayerHandFaceDownAndReturnAtNextTurnEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerHandFaceDownAndReturnAtNextTurnEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s hand is already empty."));
            return;
        }

        List<Card> exiledCards = new ArrayList<>(hand);
        hand.clear();
        for (Card card : exiledCards) {
            exileService.exileCardFaceDown(gameData, targetPlayerId, card, null);
        }

        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.endStep(ActivePlayer.CHOSEN_PLAYER),
                        new ReturnExiledCardsToTargetPlayerHandEffect(
                                targetPlayerId, exiledCards.stream().map(Card::getId).toList())),
                entry.getSourcePermanentId(), null, targetPlayerId,
                DelayedReferents.NONE.afterTurn(gameData.turnNumber)));
        gameLogService.append(gameData, GameLog.text(playerName + "'s hand is exiled face down ("
                + exiledCards.size() + " card" + (exiledCards.size() != 1 ? "s" : "") + ")."));
        log.info("Game {} - {}'s hand ({} cards) exiled face down until their next turn's end step",
                gameData.id, playerName, exiledCards.size());
    }
}
