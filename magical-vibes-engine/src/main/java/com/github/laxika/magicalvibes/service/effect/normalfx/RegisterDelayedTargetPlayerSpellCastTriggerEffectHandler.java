package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedTargetPlayerSpellCastTriggerEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gives the player the resolving object targets the ability "whenever you cast" a matching spell, which that player
 * controls (the boon they get, the cascade of the next spell they cast), with the resolving object's source as its
 * source, for the registration's duration (CR 603.7b).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedTargetPlayerSpellCastTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedTargetPlayerSpellCastTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedTargetPlayerSpellCastTriggerEffect) effect;
        if (entry.getTargetId() == null || !gameData.playerIds.contains(entry.getTargetId())) {
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(e.duration().delayed(entry.getTargetId(), entry.getCard(),
                e.delayedAbility(), entry.getSourcePermanentId(), gameData.turnNumber, null));
        log.info("Game {} - {} gives the targeted player a delayed spell-cast ability ({})",
                gameData.id, entry.getCard().getName(), e.duration());
    }
}
