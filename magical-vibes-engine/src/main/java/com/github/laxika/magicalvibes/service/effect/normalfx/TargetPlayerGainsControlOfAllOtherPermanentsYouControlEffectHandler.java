package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfAllOtherPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves targeted permanent donation effects such as Sky Swallower's ETB ability. */
@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfAllOtherPermanentsYouControlEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfAllOtherPermanentsYouControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID newControllerId = entry.getTargetId();
        UUID currentControllerId = entry.getControllerId();
        if (newControllerId == null || !gameData.playerIds.contains(newControllerId)
                || newControllerId.equals(currentControllerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(currentControllerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toGive = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!permanent.getCard().getId().equals(entry.getCard().getId())) {
                toGive.add(permanent);
            }
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(ControlDuration.PERMANENT);
        for (Permanent permanent : toGive) {
            creatureControlService.applyControlEffect(gameData, newControllerId, permanent,
                    controlEffect, EffectDuration.PERMANENT, null, entry.getCard().getName());
        }
    }
}
