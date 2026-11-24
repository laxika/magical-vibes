package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SuppressStaticEffectUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SuppressStaticEffectUntilEndOfTurnEffect}: a layer-6 removal on each permanent its subject names, at
 * this effect's timestamp, so an ability another permanent grants afterwards (a Guildhouse entering later) is newer
 * and stays (CR 613.7).
 */
@Component
@RequiredArgsConstructor
public class SuppressStaticEffectUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SuppressStaticEffectUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var suppress = (SuppressStaticEffectUntilEndOfTurnEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, suppress.subject())) {
            gameData.addEffectOnPermanent(entry, effect, permanent.getId(), Duration.UNTIL_END_OF_TURN);
        }
    }
}
