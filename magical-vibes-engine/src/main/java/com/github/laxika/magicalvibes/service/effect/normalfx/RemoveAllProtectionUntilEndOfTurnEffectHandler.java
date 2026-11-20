package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveAllProtectionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllProtectionUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllProtectionUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveAllProtectionUntilEndOfTurnEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        if (remove.scope() == GrantScope.ALL_PERMANENTS) {
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(GrantScope.ALL_PERMANENTS.subject(), remove.filter()))) {
                removeFrom(gameData, entry, remove, permanent);
            }
            return;
        }
        if (remove.scope() != GrantScope.SELF && remove.scope() != GrantScope.TARGET) {
            return;
        }

        Permanent target = subjectResolutionService.permanent(gameData, entry, effect, remove.scope().subject());
        if (target != null && (remove.filter() == null
                || predicateEvaluationService.matchesPermanentPredicate(target, remove.filter(), filterContext))) {
            removeFrom(gameData, entry, remove, target);
        }
    }

    private void removeFrom(GameData gameData, StackEntry entry,
                            RemoveAllProtectionUntilEndOfTurnEffect remove, Permanent target) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), remove, target.getId(), null, null,
                Duration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" loses protection until end of turn.")
                .build());
        log.info("Game {} - {} loses protection until end of turn",
                gameData.id, target.getCard().getName());
    }
}
