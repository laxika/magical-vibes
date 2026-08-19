package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProliferateEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProliferateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        ProliferateEffect typedEffect = (ProliferateEffect) effect;
        // A null amount is the ordinary single event. The dynamic form is used when a preceding
        // resolution step records a variable number of proliferates on the stack entry.
        int totalProliferates = typedEffect.amount() == null
                ? (int) entry.getEffectsToResolve().stream()
                        .filter(e -> e instanceof ProliferateEffect)
                        .count()
                : Math.max(0, amountEvaluationService.evaluate(gameData, typedEffect.amount(),
                        AmountContext.forStackEntry(entry, null)));
        totalProliferates = gameQueryService.replaceProliferateCount(
                gameData, controllerId, totalProliferates);
        if (totalProliferates <= 0) {
            return;
        }
        triggerCollectionService.checkProliferateTriggers(gameData, controllerId, totalProliferates);

        // Collect all permanents with counters (any player's battlefield).
        List<UUID> eligiblePermanentIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0
                    || p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                    || p.getCounterCount(CounterType.LOYALTY) > 0
                    || p.getCounterCount(CounterType.SLIME) > 0
                    || p.getCounterCount(CounterType.HATCHLING) > 0
                    || p.getCounterCount(CounterType.AWAKENING) > 0
                    || p.getCounterCount(CounterType.AIM) > 0) {
                eligiblePermanentIds.add(p.getId());
            }
        });

        List<UUID> eligiblePlayerIds = new ArrayList<>();
        for (UUID playerId : gameData.playerIds) {
            if (gameData.playerPoisonCounters.getOrDefault(playerId, 0) > 0) {
                eligiblePlayerIds.add(playerId);
            }
        }

        if (eligiblePermanentIds.isEmpty() && eligiblePlayerIds.isEmpty()) {
            String logEntry = "Proliferate: no permanents or players with counters to choose.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - Proliferate: no eligible permanents or players", gameData.id);
            return;
        }

        MultiPermanentChoiceContext.Proliferate context =
                new MultiPermanentChoiceContext.Proliferate(totalProliferates);
        int maxCount = eligiblePermanentIds.size() + eligiblePlayerIds.size();
        if (eligiblePlayerIds.isEmpty()) {
            playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligiblePermanentIds,
                    maxCount, context, "Proliferate: Choose permanents to add counters to.");
        } else {
            playerInputService.beginMultiPermanentOrPlayerChoice(gameData, controllerId,
                    eligiblePermanentIds, eligiblePlayerIds, maxCount, context,
                    "Proliferate: Choose permanents and/or players to add counters to.");
        }
    }
}
