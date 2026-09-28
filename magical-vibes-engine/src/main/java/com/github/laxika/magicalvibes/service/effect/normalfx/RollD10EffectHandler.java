package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollD10Effect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RollD10EffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final DiceRollService diceRollService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollD10Effect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollD10Effect rollEffect = (RollD10Effect) effect;
        int result = diceRollService.roll(10);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " rolls a d10 for " + entry.getCard().getName() + ": " + result + "."));
        entry.setEventValue(result);
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, entry.getControllerId(), 1, result);
        triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                gameData, entry.getControllerId(), 10, result);

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
            entry.insertEffectsToResolve(effectIndex + 1, List.of(rollEffect.onResult()));
        }
    }
}
