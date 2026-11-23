package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Floats the granted static effect on each player the grant names, for its duration. */
@Component
@RequiredArgsConstructor
public class GrantStaticEffectToPlayerEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToPlayerEffect) effect;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, grant.player())) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    null,
                    entry.getControllerId(),
                    grant.staticEffect(),
                    null,
                    playerId,
                    null,
                    grant.duration(),
                    0L));
        }
    }
}
