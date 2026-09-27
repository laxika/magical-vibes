package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentDamagedPlayerControlsEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Fallback resolver for the trigger-only damaged-player control effect. Combat and damage trigger
 * collectors normally bind the damaged player and queue the resulting targeted control effect
 * before this handler is needed.
 */
@Component
public class GainControlOfPermanentDamagedPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfPermanentDamagedPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damagedEffect = (GainControlOfPermanentDamagedPlayerControlsEffect) effect;
        UUID damagedPlayerId = entry.getAttackedTargetId() != null
                ? entry.getAttackedTargetId() : entry.getTargetId();
        if (damagedPlayerId == null) return;

        gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                entry.getCard(), entry.getControllerId(),
                List.of(damagedEffect.forDamagedPlayer(damagedPlayerId)),
                entry.getSourcePermanentId(), entry.getControllerId(), damagedPlayerId));
    }
}
