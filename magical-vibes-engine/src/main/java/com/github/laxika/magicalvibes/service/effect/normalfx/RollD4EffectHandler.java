package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollD4Effect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RollD4EffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final D4RollService d4RollService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollD4Effect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollD4Effect rollEffect = (RollD4Effect) effect;
        int result = d4RollService.roll(gameData, entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " rolls a d4 for " + entry.getCard().getName() + ": " + result + "."));
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, entry.getControllerId(), 1, result);
        if (result != 1) {
            return;
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
            entry.insertEffectsToResolve(effectIndex + 1, List.of(rollEffect.onOne()));
        }
    }
}
