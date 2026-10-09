package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollD8Effect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RollD8EffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final DiceRollService diceRollService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollD8Effect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollD8Effect rollEffect = (RollD8Effect) effect;
        int result = diceRollService.roll(8);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " rolls a d8 for " + entry.getCard().getName() + ": " + result + "."));
        entry.setEventValue(result);
        entry.setXValue(result);
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, entry.getControllerId(), 1, result);
        triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                gameData, entry.getControllerId(), 8, result);

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
            if (rollEffect.activationCost()) {
                entry.replaceEffectToResolve(effectIndex, rollEffect.onResult());
            } else {
                entry.insertEffectsToResolve(effectIndex + 1, List.of(rollEffect.onResult()));
            }
        }
    }
}
