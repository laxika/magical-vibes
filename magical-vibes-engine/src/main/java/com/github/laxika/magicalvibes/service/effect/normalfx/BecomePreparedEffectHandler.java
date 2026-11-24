package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link BecomePreparedEffect}: the permanent its subject names becomes prepared, the prepare copy going to
 * that permanent's controller.
 */
@Component
@RequiredArgsConstructor
public class BecomePreparedEffectHandler implements NormalEffectHandlerBean,
        com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean {

    private final GameQueryService gameQueryService;
    private final PreparedSupport preparedSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomePreparedEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        if (effect instanceof BecomePreparedEffect prepared && prepared.asTriggeredAbility()) {
            return;
        }
        preparedSupport.preparePermanent(gameData, enteringPermanent, controllerId);
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BecomePreparedEffect preparedEffect = (BecomePreparedEffect) effect;
        if (preparedEffect.subject() instanceof PermanentSubject.Granter granter
                && granter.boundPermanentId() == null) {
            // A granted ability always knows its granter; one that doesn't is an engine error.
            entry.requireGrantingPermanentId();
        }
        Permanent prepared = subjectResolutionService.permanent(gameData, entry, effect, preparedEffect.subject());
        if (prepared == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, prepared.getId());
        preparedSupport.preparePermanent(gameData, prepared,
                controllerId != null ? controllerId : entry.getControllerId());
    }
}
