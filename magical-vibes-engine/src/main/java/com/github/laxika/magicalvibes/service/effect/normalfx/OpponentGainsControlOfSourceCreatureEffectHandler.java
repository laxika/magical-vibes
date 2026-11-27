package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link OpponentGainsControlOfSourceCreatureEffect}: an opponent of the ability's
 * controller, chosen by that controller as it resolves, gains control of the source permanent. Also reachable as a
 * {@link com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect} branch.
 */
@Component
@RequiredArgsConstructor
public class OpponentGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (OpponentGainsControlOfSourceCreatureEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        // "An opponent gains control": the controller chooses which as it resolves (the Rogue Skycaptain ruling).
        UUID opponentId = e.opponent() != null
                ? subjectResolutionService.player(gameData, entry, effect, e.opponent())
                : ChoosePlayerEffectHandler.opponentToBeChosenFirst(gameData, entry,
                        new OpponentGainsControlOfSourceCreatureEffect(e.duration(),
                                Subjects.playersThisWay(StepKind.CHOOSE))) ? null
                : ChoosePlayerEffectHandler.onlyOpponent(gameData, entry.getControllerId());
        if (opponentId == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, opponentId, source,
                new GainControlOfTargetEffect(e.duration()),
                Duration.PERMANENT, null, entry.getCard().getName());
    }
}
