package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link MakeCreatureUnblockableEffect}: each permanent its subject names that is still on the battlefield
 * can't be blocked for the effect's duration, a floating rule on that permanent (CR 611.2a, 613.11), not an ability it
 * gains, so a later "loses all abilities" leaves it. An illegal target is not affected (CR 608.2b); a source or host
 * that left is not affected either. "By creatures your opponents control" floats that restriction instead, unless the
 * ability's controller controls the permanent: then only those opponents' creatures could block it, so it simply can't
 * be blocked.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MakeCreatureUnblockableEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatureUnblockableEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unblockable = (MakeCreatureUnblockableEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, unblockable.subject())) {
            if (unblockable.opponentsCreaturesOnly()) {
                makeUnblockableByOpponentsOf(gameData, permanent, entry, unblockable.duration());
            } else {
                makeUnblockable(gameData, entry, permanent, unblockable.duration());
            }
        }
    }

    /**
     * The target can't be blocked by creatures controlled by the opponents of the resolving ability's
     * controller for {@code duration}. A creature that controller controls is blocked only by those opponents'
     * creatures, so it simply can't be blocked. Like "can't be blocked", it is a rule the effect makes, not an ability of
     * the target (CR 611.2c), so it floats as a {@link GrantNonAbilityEffectEffect} that "loses all abilities" leaves.
     */
    private void makeUnblockableByOpponentsOf(GameData gameData, Permanent target, StackEntry entry, Duration duration) {
        UUID abilityControllerId = entry.getControllerId();
        if (abilityControllerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            makeUnblockable(gameData, entry, target, duration);
            return;
        }
        List<PermanentPredicate> opponentControlled = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(abilityControllerId))
                .<PermanentPredicate>map(PermanentControlledByPlayerPredicate::new)
                .toList();
        if (opponentControlled.isEmpty()) return;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), FaceDownVisibility.sourceName(gameData, entry), null, abilityControllerId,
                new GrantNonAbilityEffectEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentAnyOfPredicate(opponentControlled)), Subjects.target()),
                target.getId(), null, null, duration, 0));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " can't be blocked by creatures its opponents control" + durationText(duration) + "."));
    }

    private void makeUnblockable(GameData gameData, StackEntry entry, Permanent target, Duration duration) {
        makeUnblockable(gameData, target, entry.getCard() == null ? null : FaceDownVisibility.sourceName(gameData, entry),
                entry.getSourcePermanentId(), entry.getControllerId(), duration);
    }

    /** "{@code target} can't be blocked this turn" (CR 611.2a), made by {@code sourceCardName}'s ability. */
    public void makeUnblockableThisTurn(GameData gameData, Permanent target, String sourceCardName,
                                        UUID sourcePermanentId, UUID controllerId) {
        makeUnblockable(gameData, target, sourceCardName, sourcePermanentId, controllerId, Duration.UNTIL_END_OF_TURN);
    }

    private void makeUnblockable(GameData gameData, Permanent target, String sourceCardName, UUID sourcePermanentId,
                                 UUID controllerId, Duration duration) {
        if (target == null) {
            return;
        }

        gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceCardName, sourcePermanentId, controllerId,
                new CantBeBlockedEffect(), target.getId(), duration));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't be blocked" + durationText(duration) + "."));

        log.info("Game {} - {} can't be blocked{}", gameData.id, target.getCard().getName(), durationText(duration));
    }

    private static String durationText(Duration duration) {
        if (Duration.UNTIL_END_OF_TURN.equals(duration)) {
            return " this turn";
        }
        return Duration.UNTIL_END_OF_COMBAT.equals(duration) ? " this combat" : "";
    }
}
