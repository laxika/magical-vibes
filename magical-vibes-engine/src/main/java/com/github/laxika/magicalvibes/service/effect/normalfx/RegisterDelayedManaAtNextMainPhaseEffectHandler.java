package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaAtNextMainPhaseEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RegisterDelayedManaAtNextMainPhaseEffect}: queues a mandatory, this-turn-only
 * {@link AddManaAtNextMainPhase} delayed action for the controller.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedManaAtNextMainPhaseEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedManaAtNextMainPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedManaAtNextMainPhaseEffect) effect;
        if (e.amount() <= 0) {
            return;
        }

        gameData.queueDelayedAction(new AddManaAtNextMainPhase(
                entry.getControllerId(), e.color(), e.amount(), entry.getCard(), false, true));

        log.info("Game {} - {} schedules {} {} at their next main phase this turn",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), e.amount(), e.color());
    }
}
