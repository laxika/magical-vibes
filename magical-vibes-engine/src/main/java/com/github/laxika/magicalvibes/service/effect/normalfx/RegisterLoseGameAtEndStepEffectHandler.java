package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseGameAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RegisterLoseGameAtEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterLoseGameAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Long extraTurnSequence = entry.getCard().getEffects(EffectSlot.SPELL)
                .stream()
                .anyMatch(ControllerExtraTurnEffect.class::isInstance)
                ? gameData.extraTurnSequences.peekFirst()
                : null;
        gameData.queueDelayedAction(
                new LoseGameAtEndStep(controllerId, entry.getCard(), gameData.turnNumber, extraTurnSequence));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers a delayed 'lose the game' at their own next end step",
                gameData.id, playerName);
    }
}
