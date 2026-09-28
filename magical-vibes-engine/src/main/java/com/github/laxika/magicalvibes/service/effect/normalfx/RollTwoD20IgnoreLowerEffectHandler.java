package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollTwoD20IgnoreLowerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RollTwoD20IgnoreLowerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final D20RollService d20RollService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollTwoD20IgnoreLowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollTwoD20IgnoreLowerEffect rollEffect = (RollTwoD20IgnoreLowerEffect) effect;
        int result = d20RollService.roll(gameData, entry.getControllerId(), 2);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " rolls two d20 for " + entry.getCard().getName() + " and keeps the higher result: "
                + result + "."));
        entry.setEventValue(result);
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, entry.getControllerId(), 1, result);
        triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                gameData, entry.getControllerId(), 20, result);
        if (result == 20) {
            triggerCollectionService.checkControllerRollsNaturalTwentyTriggers(gameData, entry.getControllerId());
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
            CardEffect branch = result <= rollEffect.lowBranchMax()
                    ? rollEffect.lowBranch() : rollEffect.highBranch();
            entry.insertEffectsToResolve(effectIndex + 1, List.of(branch));
        }
    }
}
