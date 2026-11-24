package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a one-shot {@link GrantSubtypeEffect}: the subtype is stored as a {@link Duration#PERMANENT} floating
 * layer-4 effect on each affected permanent that is still on the battlefield, stamped as it is made. {@code SELF}
 * affects the source, {@code TARGET} the targets, {@code TRIGGERING_PERMANENT} the permanent the trigger event
 * was about ("that creature becomes a Vampire in addition to its other types"), and {@code Subjects.thisWay(CHOOSE)}
 * the permanent an earlier step chose (amass: "if it isn't a Zombie, it becomes a Zombie in addition to its other
 * types", CR 701.47a); the last two are logged.
 */
@Component
@RequiredArgsConstructor
public class GrantSubtypeEffectResolutionHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantSubtypeEffect grant = (GrantSubtypeEffect) effect;
        if (!(grant.subject() instanceof PermanentSubject.Source)
                && !(grant.subject() instanceof PermanentSubject.Triggering)
                && !(grant.subject() instanceof PermanentSubject.Target)
                && !(grant.subject() instanceof PermanentSubject.ThisWay)) {
            return;
        }
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, grant.subject())) {
            gameData.addEffectOnPermanent(entry, grant, target.getId(), Duration.PERMANENT);
            if (grant.subject() instanceof PermanentSubject.Triggering
                    || grant.subject() instanceof PermanentSubject.ThisWay) {
                gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                        .text(" becomes a " + grant.subtype().getDisplayName() + " in addition to its other types.")
                        .build());
            }
        }
    }
}
