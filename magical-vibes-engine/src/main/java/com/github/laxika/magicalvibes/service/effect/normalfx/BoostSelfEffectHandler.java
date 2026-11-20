package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessBoostEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
 * Resolves {@link BoostSelfEffect}: the permanent its subject names (through {@link SubjectResolutionService}; nothing
 * if it has left the battlefield, CR 400.7) gets floating layer-7c boosts and keyword grants for the effect's duration,
 * the amounts read with that permanent as the amount source.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoostSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostSelfEffect) effect;
        Permanent boosted = subjectResolutionService.permanent(gameData, entry, effect, boost.subject());
        if (boosted == null) {
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, boosted);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);

        if (Duration.UNTIL_END_OF_TURN.equals(boost.duration())) {
            gameData.addPowerToughnessBoost(entry, boosted.getId(), powerBoost, toughnessBoost);
        } else {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new PowerToughnessBoostEffect(powerBoost, toughnessBoost),
                    boosted.getId(), null, null, boost.duration(), 0));
        }
        if (!boost.keywords().isEmpty()) {
            gameData.addEffectOnPermanent(entry, new GrantKeywordEffect(boost.keywords(), GrantScope.TARGET),
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

    private static String logText(BoostSelfEffect boost, int powerBoost, int toughnessBoost) {
        List<String> parts = new ArrayList<>();
        if (boost.keywords().isEmpty() || powerBoost != 0 || toughnessBoost != 0) {
            parts.add(String.format("gets %+d/%+d", powerBoost, toughnessBoost));
        }
        if (!boost.keywords().isEmpty()) {
            parts.add("gains " + boost.keywords().stream()
                    .map(BoostSelfEffectHandler::keywordName)
                    .sorted()
                    .collect(Collectors.joining(" and ")));
        }
        return " " + String.join(" and ", parts) + " "
                + (Duration.UNTIL_END_OF_COMBAT.equals(boost.duration()) ? "until end of combat" : "until end of turn")
                + ".";
    }

    private static String keywordName(Keyword keyword) {
        String name = keyword.name();
        return name.charAt(0) + name.substring(1).toLowerCase().replace('_', ' ');
    }
}
