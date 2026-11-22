package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileTargetPlayerHandEffect}: every card in the hand of each player its subject names (APNAP order)
 * is exiled. There is no player choice and no play permission; exiling from hand is not a discard, so no discard
 * triggers fire. Mirrors the graveyard-exile log strings, one line per player.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTargetPlayerHandEffect exileEffect = (ExileTargetPlayerHandEffect) effect;
        UUID sourcePermanentId = exileEffect.trackWithSource()
                ? resolveSourcePermanentId(gameData, entry)
                : null;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, exileEffect.players())) {
            exileHand(gameData, entry, playerId, sourcePermanentId, exileEffect.faceDown());
        }
    }

    private void exileHand(GameData gameData, StackEntry entry, UUID playerId, UUID sourcePermanentId,
                           boolean faceDown) {
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s hand is already empty."));
            return;
        }

        int count = hand.size();
        List<Card> toExile = new ArrayList<>(hand);
        hand.clear();
        for (Card card : toExile) {
            // Exiled cards are kept per owner.
            UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : playerId;
            if (faceDown) {
                exileService.exileCardFaceDown(gameData, ownerId, card, sourcePermanentId);
            } else if (sourcePermanentId == null) {
                exileService.exileCard(gameData, ownerId, card);
            } else {
                exileService.exileCard(gameData, ownerId, card, sourcePermanentId);
            }
        }

        entry.addToStep(StepKind.EXILE,
                StepOutcome.ofObjects(toExile.stream().map(Card::getId).toList(), List.of(playerId)));
        String logEntry = playerName + "'s hand is exiled" + (faceDown ? " face down (" : " (") + count + " card" + (count != 1 ? "s" : "") + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {}'s hand ({} cards) exiled", gameData.id, playerName, count);
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
