package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantFreeCastForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Floats {@link GrantFreeCastForNextMatchingSpellEffect} on the controller until a matching spell is cast, with the
 * chosen values of its filter written in as it resolves ("of the chosen type": the source's choice as it is or last was,
 * CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class GrantFreeCastForNextMatchingSpellEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFreeCastForNextMatchingSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantFreeCastForNextMatchingSpellEffect) effect;
        gameData.addPlayerEffect(entry, new GrantFreeCastForNextMatchingSpellEffect(
                        subjectResolutionService.bindChosenValues(gameData, entry, effect, grant.predicate())),
                entry.getControllerId(), Duration.UNTIL_MATCHING_SPELL_CAST);
    }
}
