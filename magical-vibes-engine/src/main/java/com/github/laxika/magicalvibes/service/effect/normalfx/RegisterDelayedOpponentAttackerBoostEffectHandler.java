package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedOpponentAttackerBoost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedOpponentAttackerBoostEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RegisterDelayedOpponentAttackerBoostEffect} by queuing the delayed action that
 * {@code CombatAttackService} fires on every opposing attacker until the controller's next turn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedOpponentAttackerBoostEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedOpponentAttackerBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedOpponentAttackerBoostEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.queueDelayedAction(
                new DelayedOpponentAttackerBoost(controllerId, e.power(), e.toughness(), entry.getCard()));
        log.info("Game {} - {} registers a delayed {}/{} boost on opposing attackers until its controller's next turn",
                gameData.id, entry.getCard().getName(), e.power(), e.toughness());
    }
}
