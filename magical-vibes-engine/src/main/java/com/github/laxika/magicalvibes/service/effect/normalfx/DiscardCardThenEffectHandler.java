package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardCardThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardCardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardCardThenEffect) effect;

        UUID discardPlayerId = resolveDiscardPlayer(gameData, entry, e.recipient());
        if (discardPlayerId == null) {
            return;
        }

        String playerName = gameData.playerIdToName.get(discardPlayerId);
        List<Card> hand = gameData.playerHands.get(discardPlayerId);

        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
            for (int i = 0; i < hand.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(hand.get(i), e.filter(), sourceCardId)) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String logEntry = playerName + " has no " + e.cardDescription() + " to discard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no {} to discard for {}",
                    gameData.id, playerName, e.cardDescription(), entry.getCard().getName());
            return;
        }

        gameData.discardCausedByOpponent = e.recipient() != DiscardRecipient.CONTROLLER;
        if (gameData.discardCausedByOpponent
                && gameQueryService.isDiscardPrevented(gameData, discardPlayerId)) {
            return;
        }
        UUID preservedTargetId = entry.getTargetId();
        if (e.useEntryTarget() && preservedTargetId == null) {
            List<UUID> effectTargets = entry.targetsForEffect(e);
            if (!effectTargets.isEmpty()) {
                preservedTargetId = effectTargets.getFirst();
            } else if (entry.getTargetIds().size() == 1) {
                preservedTargetId = entry.getTargetIds().getFirst();
            }
        }
        Permanent currentSource = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sourceSnapshot = currentSource == null
                ? entry.getSourcePermanentSnapshot() : new Permanent(currentSource);
        DiscardFollowUp followUp = DiscardFollowUp.thenEffect(entry.getCard(),
                        e.useEntryTarget() && preservedTargetId == null ? null : e.thenEffect(),
                        e.condition(), e.useEntryTarget() ? preservedTargetId : null,
                        e.alternateCardType(), e.alternateThenEffect())
                .withSourceContext(entry.getSourcePermanentId(),
                        sourceSnapshot, entry.getEventValue());
        playerInputService.beginDiscardChoice(gameData, discardPlayerId, validIndices,
                entry.getCard().getName() + " — Choose " + e.cardDescription() + " to discard.",
                1, followUp);

        String logEntry = playerName + " is choosing " + e.cardDescription() + " to discard.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} choosing {} to discard for {}",
                gameData.id, playerName, e.cardDescription(), entry.getCard().getName());
    }

    private UUID resolveDiscardPlayer(GameData gameData, StackEntry entry, DiscardRecipient recipient) {
        return switch (recipient) {
            case CONTROLLER -> entry.getControllerId();
            case TARGET_PLAYER -> entry.getTargetId() != null && gameData.playerIds.contains(entry.getTargetId())
                    ? entry.getTargetId() : null;
            case TARGET_PERMANENT_CONTROLLER, TARGET_PLAYER_OR_PERMANENT_CONTROLLER -> {
                UUID targetId = entry.getTargetId();
                if (targetId == null) {
                    yield null;
                }
                yield gameData.playerIds.contains(targetId)
                        ? targetId : gameQueryService.findPermanentController(gameData, targetId);
            }
            default -> entry.getControllerId();
        };
    }
}
