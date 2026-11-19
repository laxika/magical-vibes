package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetIfDidNotAttackAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Makes the delayed triggered ability "at the beginning of the next end step, if it didn't attack this turn, destroy
 * it" about the target (CR 603.7a): its "if" is an intervening "if" (CR 603.4), checked as the end step begins and
 * again as the ability resolves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyTargetIfDidNotAttackAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetIfDidNotAttackAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        TriggerOptions didNotAttack = TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(
                new PermanentNotPredicate(new PermanentAttackedThisTurnPredicate())));
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY), didNotAttack,
                        new DestroyTargetPermanentEffect(Subjects.triggering())),
                entry.getSourcePermanentId(), target.getId(), null,
                DelayedReferents.permanents(List.of(target.getId()))));

        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(),
                        " will be destroyed at the beginning of the next end step if it doesn't attack this turn."));
        log.info("Game {} - {} scheduled for conditional end-step destruction if it doesn't attack",
                gameData.id, target.getCard().getName());
    }
}
