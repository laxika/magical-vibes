package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LosesAllAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LosesAllAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LosesAllAbilitiesEffect) effect;
        PermanentSubject subject = e.subject();
        if (subject.declaresPlayerTarget() || Subjects.creaturesYouControl().equals(subject)) {
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(subject, e.filter()))) {
                applyEffect(gameData, entry, e, permanent);
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) lose all abilities until end of turn.").build());
            return;
        }

        if (subject.isSet()) {
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(subject, e.filter()))) {
                if (applyEffect(gameData, entry, e, permanent)) {
                    count++;
                }
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) lose all abilities until end of turn.").build());
            return;
        }

        if (!(subject instanceof PermanentSubject.Source) && !(subject instanceof PermanentSubject.Target)
                && !(subject instanceof PermanentSubject.TargetAbilitySource)) {
            return;
        }

        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
            if (!applyEffect(gameData, entry, e, target)) {
                continue;
            }

            Duration duration = e.duration();
            String durationText = Duration.STATIC.equals(duration) || Duration.PERMANENT.equals(duration)
                    ? "indefinitely"
                    : Duration.UNTIL_YOUR_NEXT_TURN.equals(duration) ? "until your next turn"
                    : duration != null && duration.asLongAs() != null
                    ? "for as long as its source remains on the battlefield"
                    : "until end of turn";
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    " loses all abilities " + durationText + "."));
            log.info("Game {} - {} loses all abilities {}", gameData.id, target.getCard().getName(), durationText);
        }
    }

    private boolean applyEffect(GameData gameData, StackEntry entry, LosesAllAbilitiesEffect e, Permanent target) {
        // A layer-6 effect with its own timestamp (CR 613.1f, 613.7): the abilities granted before it are removed,
        // those granted after it stay.
        boolean sourceLinked = Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(e.duration())
                || Duration.WHILE_SOURCE_REMAINS.equals(e.duration())
                || Duration.WHILE_SOURCE_TAPPED.equals(e.duration())
                || Duration.WHILE_SOURCE_REMAINS_TAPPED.equals(e.duration())
                || Duration.WHILE_ATTACHED.equals(e.duration());
        UUID sourcePermanentId = sourceLinked ? entry.getSourcePermanentId() : null;
        if (sourceLinked) {
            if (sourcePermanentId == null
                    || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                return false;
            }
            if (Duration.WHILE_SOURCE_REMAINS_TAPPED.equals(e.duration())
                    && !gameQueryService.findPermanentById(gameData, sourcePermanentId).isTapped()) {
                // The source untapped before this resolved, so the effect never begins.
                return false;
            }
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), sourcePermanentId, entry.getControllerId(), e,
                target.getId(), null, null,
                Duration.STATIC.equals(e.duration())
                        ? Duration.PERMANENT : e.duration(),
                0));
        return true;
    }
}
