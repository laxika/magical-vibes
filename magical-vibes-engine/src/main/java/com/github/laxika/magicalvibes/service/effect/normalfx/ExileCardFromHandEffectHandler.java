package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

/** Asks the player for the card; the shared hand-exile answer exiles it and adds it to the step. */
@Component
@RequiredArgsConstructor
public class ExileCardFromHandEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCardFromHandEffect) effect;
        UUID playerId = subjectResolutionService.player(gameData, entry, e, e.player());
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (e.filter() == null || predicateEvaluationService.matchesCardPredicate(hand.get(i), e.filter(),
                    sourceCardId, gameData, playerId, entry.getSourcePermanentId(), null, entry.getXValue())) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " has no card to exile from hand."));
            return;
        }
        if (e.optional()) {
            // The hand choice that may be declined: the player picks from their own hand, which nobody else sees, and
            // its answer records the exile as the hand-exile answer does.
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                    playerId, playerId, validIndices, 1, false, true, List.of(), null,
                    "You may choose a card to exile.", false, true, false));
            return;
        }
        playerInputService.beginExileOneCardFromHandChoice(gameData, playerId, validIndices, e.faceDown());
    }
}
