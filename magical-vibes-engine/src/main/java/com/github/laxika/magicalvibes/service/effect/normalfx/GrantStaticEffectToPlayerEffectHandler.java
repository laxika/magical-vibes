package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Floats the granted static effect on each player the grant names, for its duration; a source-linked duration keeps
 * the source on the floating effect so the duration clock can end it.
 */
@Component
@RequiredArgsConstructor
public class GrantStaticEffectToPlayerEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToPlayerEffect) effect;
        UUID sourcePermanentId = null;
        if (grant.duration().isSourceLinked()) {
            // A "for as long as" grant whose source already left never begins (CR 611.2b).
            sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                return;
            }
        }
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, grant.player())) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    sourcePermanentId,
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
