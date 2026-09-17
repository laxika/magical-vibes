package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollDiceEffect;
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
public class RollDiceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final DiceRollService diceRollService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollDiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollDiceEffect rollEffect = (RollDiceEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int diceCount = amountEvaluationService.evaluate(gameData, rollEffect.diceCount(),
                AmountContext.forStackEntry(entry, source));
        if (diceCount <= 0) {
            return;
        }

        List<Integer> results = new ArrayList<>(diceCount);
        List<CardEffect> branchEffects = new ArrayList<>(diceCount);
        for (int i = 0; i < diceCount; i++) {
            int result = diceRollService.roll(rollEffect.sides());
            results.add(result);
            CardEffect branch = result % 2 == 1 ? rollEffect.oddResult() : rollEffect.evenResult();
            if (branch != null) {
                branchEffects.add(branch);
            }
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " rolls " + diceCount + " d" + rollEffect.sides() + " for "
                + entry.getCard().getName() + ": " + results + "."));
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, entry.getControllerId(), diceCount);

        if (branchEffects.isEmpty()) {
            return;
        }

        int effectIndex = findEffectIndex(entry, effect);
        if (effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, branchEffects);
        }
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            if (entry.getEffectsToResolve().get(i) == effect) {
                return i;
            }
        }
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            CardEffect current = entry.getEffectsToResolve().get(i);
            if (current instanceof MayEffect may && may.wrapped() == effect) {
                return i;
            }
        }
        return -1;
    }
}
