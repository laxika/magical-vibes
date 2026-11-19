package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class BoostReferencedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BoostReferencedPermanentEffect boost = (BoostReferencedPermanentEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent referenced = subjectResolutionService.permanent(
                gameData, entry, effect, Subjects.of(boost.reference()));
        if (referenced == null) {
            return;
        }

        var context = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), context);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context);
        gameData.addPowerToughnessBoost(entry, referenced.getId(), powerBoost, toughnessBoost);
        if (!boost.keywords().isEmpty()) {
            gameData.addEffectOnPermanent(entry, new GrantKeywordEffect(Set.copyOf(boost.keywords()), GrantScope.TARGET),
                    referenced.getId(), Duration.UNTIL_END_OF_TURN);
        }
        // As for a targeted pump: the abilities of the boosted permanent already on the stack see it with the boost
        // as their last-known source (Ogre Battledriver's pump of a creature whose own enters ability waits).
        gameData.stack.stream()
                .filter(stackEntry -> referenced.getId().equals(stackEntry.getSourcePermanentId()))
                .forEach(stackEntry -> {
                    Permanent previous = stackEntry.getSourcePermanentSnapshot();
                    Permanent refreshed = new Permanent(referenced);
                    if (previous != null && previous.getId().equals(referenced.getId())) {
                        refreshed.setTransformationSequence(previous.getTransformationSequence());
                    }
                    stackEntry.setSourcePermanentSnapshot(refreshed);
                });

        gameLogService.append(gameData, GameLog.builder()
                .card(referenced.getCard())
                .text(String.format(" gets %+d/%+d until end of turn.", powerBoost, toughnessBoost))
                .build());
        log.info("Game {} - {} gets {}/{}", gameData.id, referenced.getCard().getName(), powerBoost, toughnessBoost);
    }
}
