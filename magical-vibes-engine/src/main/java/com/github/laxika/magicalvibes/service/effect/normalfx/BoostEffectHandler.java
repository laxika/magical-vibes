package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolves {@link BoostEffect}: each permanent its subject names gets a floating layer-7c boost (CR 613.4c) for the
 * effect's duration (CR 611.2a), and the keywords, the amounts found once as the effect is applied (CR 608.2h).
 *
 * <ul>
 *   <li>{@link BoostEffect.AmountsRead#BOOSTED}: the one permanent the subject names (nothing if it left, CR 400.7),
 *       the amounts read with it as their source.</li>
 *   <li>{@link BoostEffect.AmountsRead#SOURCE}: the amounts read from the effect's source, live or as it last existed;
 *       a target subject boosts each of the effect's targets still there (CR 608.2b), any other subject the creatures
 *       it names, found once (CR 611.2c).</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostEffect) effect;
        if (boost.amountsRead() == BoostEffect.AmountsRead.BOOSTED) {
            Permanent boosted = subjectResolutionService.permanent(gameData, entry, effect, boost.subject());
            if (boosted == null) {
                return;
            }
            AmountContext ctx = AmountContext.forStackEntry(entry, boosted);
            applyBoost(gameData, entry, boosted, boost,
                    amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx),
                    amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx));
            return;
        }

        // Counting contexts ("you control", "this creature's power") refer to the effect's source, not the boosted
        // creature: the source as it is, or as it last existed (CR 608.2h).
        AmountContext ctx = AmountContext.forStackEntry(entry,
                gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()));
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);

        if (!(boost.subject() instanceof PermanentSubject.Target)) {
            // The target and the creatures named with it, found once as the effect resolves (CR 611.2c).
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, boost.subject())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    applyBoost(gameData, entry, permanent, boost, powerBoost, toughnessBoost);
                }
            }
            return;
        }

        // Each target of this effect's target group, or the whole flat list when the effect isn't bound to a group.
        List<UUID> targetIds = boost.targetGroup() >= 0
                ? entry.targetsForGroup(boost.targetGroup())
                : entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && boost.targetGroup() < 0 && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                applyBoost(gameData, entry, target, boost, powerBoost, toughnessBoost);
            }
        }
    }

    /** {@code boost} applied to {@code target} alone, its amounts read from the entry's source (positional boosts). */
    void resolveForTarget(GameData gameData, StackEntry entry, Permanent target, BoostEffect boost) {
        AmountContext ctx = AmountContext.forStackEntry(entry,
                gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()));
        applyBoost(gameData, entry, target, boost,
                amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx),
                amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx));
    }

    private void applyBoost(GameData gameData, StackEntry entry, Permanent boosted, BoostEffect boost,
                            int powerBoost, int toughnessBoost) {
        gameData.addPowerToughnessBoost(FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                entry.getControllerId(), boosted.getId(), powerBoost, toughnessBoost, boost.duration());
        if (!boost.keywords().isEmpty()) {
            gameData.addEffectOnPermanent(entry, new GrantKeywordEffect(boost.keywords(), Subjects.target()),
                    boosted.getId(), boost.duration());
        }
        if (!(boost.subject() instanceof PermanentSubject.Source)) {
            refreshWaitingAbilitiesOf(gameData, boosted);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(boosted.getCard())
                .text(logText(boost, powerBoost, toughnessBoost))
                .build());
        log.info("Game {} - {} gets {}/{}", gameData.id, boosted.getCard().getName(), powerBoost, toughnessBoost);
    }

    /**
     * The abilities of the boosted permanent already on the stack see it with the boost as their last-known source
     * (Ogre Battledriver's pump of a creature whose own enters ability waits).
     */
    private static void refreshWaitingAbilitiesOf(GameData gameData, Permanent boosted) {
        gameData.stack.stream()
                .filter(stackEntry -> boosted.getId().equals(stackEntry.getSourcePermanentId()))
                .forEach(stackEntry -> {
                    Permanent previous = stackEntry.getSourcePermanentSnapshot();
                    Permanent refreshed = new Permanent(boosted);
                    if (previous != null && previous.getId().equals(boosted.getId())) {
                        refreshed.setTransformationSequence(previous.getTransformationSequence());
                    }
                    stackEntry.setSourcePermanentSnapshot(refreshed);
                });
    }

    private static String logText(BoostEffect boost, int powerBoost, int toughnessBoost) {
        List<String> parts = new ArrayList<>();
        if (boost.keywords().isEmpty() || powerBoost != 0 || toughnessBoost != 0) {
            parts.add(String.format("gets %+d/%+d", powerBoost, toughnessBoost));
        }
        if (!boost.keywords().isEmpty()) {
            parts.add("gains " + boost.keywords().stream()
                    .map(BoostEffectHandler::keywordName)
                    .sorted()
                    .collect(Collectors.joining(" and ")));
        }
        return " " + String.join(" and ", parts) + " " + durationText(boost.duration()) + ".";
    }

    private static String durationText(Duration duration) {
        if (Duration.UNTIL_END_OF_COMBAT.equals(duration)) {
            return "until end of combat";
        }
        if (Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)) {
            return "until your next turn";
        }
        return Duration.PERMANENT.equals(duration) ? "indefinitely" : "until end of turn";
    }

    private static String keywordName(Keyword keyword) {
        String name = keyword.name();
        return name.charAt(0) + name.substring(1).toLowerCase().replace('_', ' ');
    }
}
