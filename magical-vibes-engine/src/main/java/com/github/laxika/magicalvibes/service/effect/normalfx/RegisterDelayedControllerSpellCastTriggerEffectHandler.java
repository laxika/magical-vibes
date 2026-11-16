package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Creates the resolving object's delayed "whenever you cast" ability (CR 603.7a) for its controller, with the resolving
 * object's source as its source (CR 603.7e), for the registration's duration (CR 603.7b).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedControllerSpellCastTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedControllerSpellCastTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedControllerSpellCastTriggerEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(e.duration().delayed(entry.getControllerId(), entry.getCard(),
                e.delayedAbility(), entry.getSourcePermanentId(), gameData.turnNumber, e.targetFilter()));
        log.info("Game {} - {} creates a delayed spell-cast ability ({})",
                gameData.id, entry.getCard().getName(), e.duration());
    }
}
