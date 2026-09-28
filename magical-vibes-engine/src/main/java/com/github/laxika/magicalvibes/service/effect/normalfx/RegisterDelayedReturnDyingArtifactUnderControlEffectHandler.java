package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingArtifactUnderControlEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the delayed return carried by Daretti, Scrap Savant's emblem. */
@Component
@Slf4j
public class RegisterDelayedReturnDyingArtifactUnderControlEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnDyingArtifactUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID artifactCardId = entry.getTriggeringCardId();
        UUID controllerId = entry.getControllerId();
        if (artifactCardId == null || controllerId == null) {
            return;
        }

        gameData.queueDelayedAction(new DelayedGraveyardToBattlefieldUnderControl(
                artifactCardId, controllerId, null, false, null, 0, null, null, true, false));
        log.info("Game {} - {} schedules an artifact to return at the next end step",
                gameData.id, entry.getDescription());
    }
}
