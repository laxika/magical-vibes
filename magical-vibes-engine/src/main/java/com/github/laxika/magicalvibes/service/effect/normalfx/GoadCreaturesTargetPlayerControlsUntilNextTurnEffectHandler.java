package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesTargetPlayerControlsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves targeted mass goad by snapshotting the target player's creatures. */
@Component
@RequiredArgsConstructor
public class GoadCreaturesTargetPlayerControlsUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreaturesTargetPlayerControlsUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var goadEach = (GoadCreaturesTargetPlayerControlsUntilNextTurnEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                Subjects.controlledBy(goadEach.player(), new PermanentIsCreaturePredicate()))) {
            GoadCreaturesUntilNextTurnSnapshotEffect goad =
                    new GoadCreaturesUntilNextTurnSnapshotEffect(new PermanentTruePredicate());
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), goad, permanent.getId(),
                    null, null, Duration.UNTIL_YOUR_NEXT_TURN, 0));
        }
    }
}
