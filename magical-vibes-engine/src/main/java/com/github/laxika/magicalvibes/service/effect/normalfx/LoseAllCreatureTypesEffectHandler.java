package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoseAllCreatureTypesEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseAllCreatureTypesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var lose = (LoseAllCreatureTypesEffect) effect;

        if (lose.subject().declaresPlayerTarget()) {
            if (subjectResolutionService.players(gameData, entry, effect, Subjects.targetPlayer()).isEmpty()) {
                return;
            }
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    lose.subject())) {
                applyTypeLoss(gameData, entry, effect, permanent);
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" makes " + count + " creature(s) lose all creature types until end of turn.").build());
            return;
        }

        Permanent target = subjectResolutionService.permanent(gameData, entry, effect, Subjects.target());
        if (target == null) {
            return;
        }
        applyTypeLoss(gameData, entry, effect, target);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " loses all creature types until end of turn."));
    }

    private void applyTypeLoss(GameData gameData, StackEntry entry, CardEffect effect, Permanent target) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                FaceDownVisibility.sourceName(gameData, entry), null, entry.getControllerId(), effect,
                target.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
    }
}
