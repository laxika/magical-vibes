package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD12AndResolveIfGreaterThanEventValueEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RollD12AndResolveIfGreaterThanEventValueEffectHandler implements NormalEffectHandlerBean {

    private final DiceRollService diceRollService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollD12AndResolveIfGreaterThanEventValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollD12AndResolveIfGreaterThanEventValueEffect rollEffect =
                (RollD12AndResolveIfGreaterThanEventValueEffect) effect;
        int result = diceRollService.roll(12);
        String sourceName = entry.getCard() == null ? "the ability" : entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData,
                GameLog.text(playerName + " rolls a d12 for " + sourceName + ": " + result + "."));
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, entry.getControllerId(), 1, result);
        triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                gameData, entry.getControllerId(), 12, result);

        if (result <= entry.getEventValue() && result != 12) {
            return;
        }
        dispatch(gameData, entry, rollEffect.wrapped());
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler != null) {
            handler.resolve(gameData, entry, effect);
        } else {
            log.warn("No handler for effect in RollD12AndResolveIfGreaterThanEventValueEffect: {}",
                    effect.getClass().getSimpleName());
        }
    }
}
