package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MindSpikeEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MindSpikeEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MindSpikeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mindSpike = (MindSpikeEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();

        List<Integer> validIndices = new ArrayList<>();
        List<Card> matchingCards = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, mindSpike.filter(), sourceCardId, gameData, targetPlayerId)) {
                validIndices.add(i);
                matchingCards.add(card);
            }
        }

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    targetName + " reveals no noncreature, nonland cards from their hand."));
            insertFollowUpEffects(entry, effect, List.of(new LoseLifeEffect(2), new DrawCardEffect(1)));
            return;
        }

        cardRevealService.revealToAllPlayers(gameData, targetPlayerId,
                GameEventFact.RevealZone.HAND, matchingCards);
        gameLogService.append(gameData, GameLog.text(
                targetName + " reveals noncreature, nonland cards from their hand."));
        insertFollowUpEffects(entry, effect, List.of(new LoseLifeEffect(2)));

        gameData.discardCausedByOpponent = !controllerId.equals(targetPlayerId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                controllerId, targetPlayerId, validIndices, 1, true, false,
                List.of(), null, "Choose a noncreature, nonland card to discard.", false, false));
    }

    private void insertFollowUpEffects(StackEntry entry, CardEffect currentEffect, List<CardEffect> followUps) {
        int effectIndex = -1;
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == currentEffect) {
                effectIndex = i;
                break;
            }
        }
        if (effectIndex < 0) {
            throw new IllegalStateException("Mind Spike effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, followUps);
    }
}
