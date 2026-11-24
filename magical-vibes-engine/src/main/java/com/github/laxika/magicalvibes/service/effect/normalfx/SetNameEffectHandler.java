package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * A resolved name change: the permanents are found once, as the effect resolves (CR 611.2c), and each carries a
 * layer-3 floating copy for the effect's duration, applied in timestamp order with the other name setters
 * (CR 613.7).
 */
@Component("normalSetNameEffectHandler")
@RequiredArgsConstructor
public class SetNameEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetNameEffect set = (SetNameEffect) effect;
        SetNameEffect floated = set.subject() instanceof PermanentSubject.Target ? set
                : new SetNameEffect(set.name(), Subjects.target(), set.duration());
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, set.subject())) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                    entry.getControllerId(), floated, target.getId(), null, null, set.duration(), 0));
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" is named " + set.name() + ".").build());
        }
    }
}
