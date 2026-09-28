package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RollD6EffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final DiceRollService diceRollService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollD6Effect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollD6Effect rollEffect = (RollD6Effect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int diceCount = amountEvaluationService.evaluate(gameData, rollEffect.diceCount(),
                AmountContext.forStackEntry(entry, source));
        if (diceCount <= 0) {
            return;
        }

        List<Integer> results = new ArrayList<>(diceCount);
        List<CardEffect> branchEffects = new ArrayList<>(diceCount);
        int trackedResultCount = 0;
        for (int i = 0; i < diceCount; i++) {
            int result = diceRollService.roll(6);
            results.add(result);
            if (rollEffect.trackedResult() != null && result == rollEffect.trackedResult()) {
                trackedResultCount++;
            }
            triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                    gameData, entry.getControllerId(), 1, result);
            triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                    gameData, entry.getControllerId(), 6, result);
            if (!rollEffect.branches().isEmpty()) {
                CardEffect branch = rollEffect.branches().get(result - 1);
                if (branch != null) {
                    branchEffects.add(branch);
                }
            }
        }
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " rolls " + diceCount + " d6 for " + entry.getCard().getName() + ": " + results + "."));
        if (rollEffect.trackedResult() != null) {
            entry.setEventValue(trackedResultCount);
        }

        int effectIndex = -1;
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            if (entry.getEffectsToResolve().get(i) == effect) {
                effectIndex = i;
                break;
            }
        }
        if (effectIndex < 0) {
            for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
                CardEffect current = entry.getEffectsToResolve().get(i);
                if (current instanceof MayEffect may && may.wrapped() == effect) {
                    effectIndex = i;
                    break;
                }
            }
        }
        if (effectIndex >= 0) {
            if (!branchEffects.isEmpty()) {
                entry.insertEffectsToResolve(effectIndex + 1, branchEffects);
            }
        }
    }
}
