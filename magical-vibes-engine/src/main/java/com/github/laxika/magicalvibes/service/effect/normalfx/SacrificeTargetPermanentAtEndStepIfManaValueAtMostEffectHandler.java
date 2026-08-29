package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Registers Angrath's mana-value-conditional delayed sacrifice. */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }

        SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect sacrifice =
                (SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect) effect;
        gameData.queueDelayedAction(new DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost(
                target.getId(), entry.getControllerId(), sacrifice.maxManaValue()));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " will be sacrificed at the beginning of the next end step if its mana value is "
                        + sacrifice.maxManaValue() + " or less."));
        log.info("Game {} - {} scheduled for conditional sacrifice at end step",
                gameData.id, target.getCard().getName());
    }
}
