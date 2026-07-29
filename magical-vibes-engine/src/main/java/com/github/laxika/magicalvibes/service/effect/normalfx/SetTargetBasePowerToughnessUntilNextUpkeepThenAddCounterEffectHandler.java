package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Cycle of Life's ability: sets the target's base P/T until the controller's next upkeep
 * and schedules the counter that arrives at that upkeep.
 *
 * <p>The base-P/T set is registered as a layer-7b {@link FloatingContinuousEffect} carrying a
 * {@link SetBasePowerToughnessEffect} payload — the same shape the one-shot setter uses, so it
 * orders against other 7b setters by timestamp — but with
 * {@link EffectDuration#UNTIL_CONTROLLERS_NEXT_UPKEEP} so it survives end-of-turn cleanup. The
 * legacy {@code basePowerToughnessOverriddenUntilEndOfTurn} fields are deliberately NOT written:
 * they are cleared during turn cleanup, which would end the effect a turn early.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target no longer on battlefield, effect fizzles", gameData.id);
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                new SetBasePowerToughnessEffect(e.power(), e.toughness()), target.getId(), null, null,
                EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP, 0));

        gameData.queueDelayedAction(new PutCounterOnPermanentAtNextUpkeep(entry.getControllerId(),
                target.getId(), e.counterType(), e.counterAmount(), entry.getCard()));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " has base power and toughness " + e.power() + "/" + e.toughness()
                        + " until its controller's next upkeep."));
        log.info("Game {} - {} base P/T set to {}/{} until next upkeep, {} counter queued",
                gameData.id, target.getCard().getName(), e.power(), e.toughness(), e.counterType());
    }
}
