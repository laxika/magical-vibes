package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCreatureCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolves {@link CantBlockThisTurnEffect}: each permanent a named subject finds gets a floating {@link CantBlockEffect}
 * until end of turn; a set becomes a floating rule over the predicate {@link SubjectResolutionService#lastingRule}
 * makes of it, which also reaches creatures entering later (CR 611.2c).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CantBlockThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantBlockThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CantBlockThisTurnEffect) effect;
        if (e.subject() instanceof PermanentSubject.ControlledBy set) {
            resolveRule(gameData, entry, e, set);
            return;
        }
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            resolveForTarget(gameData, entry, permanent);
        }
    }

    void resolveForTarget(GameData gameData, StackEntry entry, Permanent target) {
        gameData.addEffectOnPermanent(entry, new CantBlockEffect(), target.getId(), Duration.UNTIL_END_OF_TURN);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't block this turn."));
        log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
    }

    private void resolveRule(GameData gameData, StackEntry entry, CantBlockThisTurnEffect effect,
                             PermanentSubject.ControlledBy set) {
        PermanentPredicate predicate = subjectResolutionService.lastingRule(gameData, entry, effect, set);
        if (predicate == null) {
            return;
        }
        predicate = lockCounterThresholds(gameData, entry.getControllerId(), predicate);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new MatchingCreaturesCantBlockMatchingCreaturesEffect(predicate,
                        new PermanentTruePredicate(), "can't block this turn"),
                null, null, predicate, Duration.UNTIL_END_OF_TURN, 0));

        if (set.players() instanceof PlayerSubject.Each each && each.relation() == PlayerSubject.Relation.ALL) {
            gameLogService.append(gameData, GameLog.text("Matching creatures can't block this turn."));
            return;
        }
        String playerNames = subjectResolutionService.players(gameData, entry, effect, set.players()).stream()
                .map(gameData.playerIdToName::get)
                .collect(Collectors.joining(" and "));
        String text = (excludesTarget(set.filter()) ? "Other creatures" : "Creatures") + " controlled by "
                + playerNames + " can't block this turn.";
        gameLogService.append(gameData, GameLog.text(text));
        log.info("Game {} - {}", gameData.id, text);
    }

    /** Whether the set leaves out the effect's target ("other creatures that player controls"). */
    private static boolean excludesTarget(PermanentPredicate filter) {
        return switch (filter) {
            case PermanentNotPredicate not -> not.predicate() instanceof PermanentIsTargetCardPredicate;
            case PermanentAllOfPredicate allOf -> allOf.predicates().stream()
                    .anyMatch(CantBlockThisTurnEffectHandler::excludesTarget);
            default -> false;
        };
    }

    /**
     * Fixes the number in a "power less than or equal to the number of {@code counters} on creatures you
     * control" filter when the effect resolves (CR 608.2h). The restriction itself does not modify
     * characteristics, so which creatures it hits is still checked continuously (CR 611.2c).
     */
    private PermanentPredicate lockCounterThresholds(GameData gameData, UUID controllerId, PermanentPredicate predicate) {
        if (predicate instanceof PermanentPowerAtMostControlledCreatureCountersPredicate countersPredicate) {
            int counterCount = 0;
            for (Permanent controlled : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
                if (gameQueryService.isCreature(gameData, controlled)) {
                    counterCount += controlled.getCounterCount(countersPredicate.counterType());
                }
            }
            return new PermanentPowerAtMostPredicate(counterCount);
        }
        if (predicate instanceof PermanentAllOfPredicate allOf) {
            return new PermanentAllOfPredicate(allOf.predicates().stream()
                    .map(inner -> lockCounterThresholds(gameData, controllerId, inner))
                    .toList());
        }
        return predicate;
    }
}
