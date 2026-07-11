package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllLandsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfAllLandsTargetPlayerControlsEffect} (Gilt-Leaf Archdruid). Gains the
 * controller permanent control of every land the target player controls at resolution, reusing the
 * layer-2 control machinery with a per-land {@link GainControlOfTargetEffect} floating effect.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfAllLandsTargetPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private static final GainControlOfTargetEffect CONTROL_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.PERMANENT);

    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfAllLandsTargetPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        for (Permanent land : new ArrayList<>(battlefield)) {
            if (!land.getCard().hasType(CardType.LAND)) continue;
            creatureControlService.applyControlEffect(gameData, entry.getControllerId(), land,
                    CONTROL_EFFECT, ControlDuration.PERMANENT.toEffectDuration(), null,
                    entry.getCard().getName());
        }
    }
}
