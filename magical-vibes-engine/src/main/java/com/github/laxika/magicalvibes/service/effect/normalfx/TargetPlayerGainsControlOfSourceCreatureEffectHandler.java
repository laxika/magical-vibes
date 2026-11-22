package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CreatureControlService creatureControlService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerGainsControlOfSourceCreatureEffect e =
                (TargetPlayerGainsControlOfSourceCreatureEffect) effect;

        UUID newControllerId = subjectResolutionService.player(gameData, entry, effect, e.player());
        if (newControllerId == null || !gameData.playerIds.contains(newControllerId)) {
            return;
        }

        Permanent permanent = subjectResolutionService.permanent(gameData, entry, effect, e.permanent());
        if (permanent == null) {
            if (e.permanent() instanceof PermanentSubject.Source) {
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s ability has no effect (it is no longer on the battlefield)."));
            }
            return;
        }

        boolean controlApplied = creatureControlService.applyControlEffect(gameData, newControllerId, permanent,
                new GainControlOfTargetEffect(e.duration()),
                e.duration(), null, entry.getCard().getName());

        if (controlApplied && e.thenEffect() != null) {
            int effectIndex = entry.getResolvingEffectIndex();
            if (effectIndex < 0) {
                effectIndex = entry.getEffectsToResolve().indexOf(effect);
            }
            if (effectIndex >= 0) {
                entry.insertEffectsToResolve(effectIndex + 1, List.of(e.thenEffect()));
            }
        }
    }
}
