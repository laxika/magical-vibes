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
 * object's source as its source (CR 603.7e), for the registration's duration (CR 603.7b), keeping the number the
 * resolution noted, if it noted one ("where X is the noted number").
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
        gameData.temporaryGlobalTriggeredAbilities.add(e.delayed(entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), gameData.turnNumber).keepingNotedAmount(entry.getResolutionResults()));
        log.info("Game {} - {} creates a delayed spell-cast ability ({})",
                gameData.id, entry.getCard().getName(), e.duration());
    }
}
