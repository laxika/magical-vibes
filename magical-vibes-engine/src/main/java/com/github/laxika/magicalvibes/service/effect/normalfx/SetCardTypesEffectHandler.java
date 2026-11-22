package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("normalSetCardTypesEffectHandler")
@RequiredArgsConstructor
public class SetCardTypesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetCardTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetCardTypesEffect set = (SetCardTypesEffect) effect;
        if (!(set.subject() instanceof PermanentSubject.Target)) {
            return;
        }
        if (Duration.WHILE_SOURCE_REMAINS.equals(set.duration())
                && (entry.getSourcePermanentId() == null
                || gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) == null)) {
            return;
        }
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, set.subject())) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                    entry.getControllerId(), set, target.getId(), null, null,
                    set.duration(), 0));
        }
    }
}
