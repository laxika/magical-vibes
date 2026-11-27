package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ApplyRuleToPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ApplyRuleToPermanentEffect}: the rule floats on each permanent its subject names that is still on
 * the battlefield, for the effect's duration (CR 611.2a), as a non-ability layer-6 payload that a later "loses all
 * abilities" leaves (CR 613.1f).
 */
@Component
@RequiredArgsConstructor
public class ApplyRuleToPermanentEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ApplyRuleToPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var apply = (ApplyRuleToPermanentEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, apply.subject())) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(
                    entry.getCard() == null ? null : FaceDownVisibility.sourceName(gameData, entry),
                    entry.getSourcePermanentId(), entry.getControllerId(),
                    new GrantNonAbilityEffectEffect(apply.rule(), Subjects.target()), permanent.getId(),
                    apply.duration()));
        }
    }
}
