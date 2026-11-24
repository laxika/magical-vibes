package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The resolved form of {@link LoseAllLandTypesEffect}: the permanents its subject names as it resolves (CR 611.2c)
 * each carry the removal as a floating layer-4 effect for its duration. An Equipment that stops being one becomes
 * unattached at once (CR 301.5c), so later steps of the same resolution see it free.
 */
@Component("normalLoseAllLandTypesEffectHandler")
@RequiredArgsConstructor
public class LoseAllLandTypesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DurationClock durationClock;
    private final GameLogService gameLogService;
    private final UnattachTriggerSupport unattachTriggerSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseAllLandTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var lose = (LoseAllLandTypesEffect) effect;
        LoseAllLandTypesEffect floated = new LoseAllLandTypesEffect(Subjects.target(), null, lose.subtypes(),
                lose.supertype(), lose.duration());
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, lose.subject())) {
            if (lose.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, lose.filter())) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                    entry.getControllerId(), floated, permanent.getId(), null, null, lose.duration(), 0));

            if (permanent.isAttached() && lose.subtypes() != null && lose.subtypes().contains(CardSubtype.EQUIPMENT)
                    && !gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.EQUIPMENT)
                    && !gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.AURA)) {
                unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, permanent, permanent.getAttachedTo());
                permanent.setAttachedTo(null);
                durationClock.sourceUnattached(gameData, permanent.getId());
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " becomes unattached."));
            }

            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                    " is no longer " + removedText(lose) + durationText(lose.duration()) + "."));
        }
    }

    private static String removedText(LoseAllLandTypesEffect lose) {
        if (lose.supertype() != null) {
            return lose.supertype().getDisplayName();
        }
        if (lose.subtypes() == null) {
            return "any land type";
        }
        return lose.subtypes().stream().map(CardSubtype::getDisplayName).sorted().collect(Collectors.joining(", "));
    }

    private static String durationText(Duration duration) {
        return Duration.UNTIL_END_OF_TURN.equals(duration) ? " until end of turn" : "";
    }
}
