package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerUntapRemoveFromCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(
                new DelayedUnblockedAttackerUntapRemoveFromCombat(controllerId, entry.getCard()));
        log.info("Game {} - {} registers delayed untap-and-remove-from-combat for unblocked attackers",
                gameData.id, entry.getCard().getName());
    }
}
