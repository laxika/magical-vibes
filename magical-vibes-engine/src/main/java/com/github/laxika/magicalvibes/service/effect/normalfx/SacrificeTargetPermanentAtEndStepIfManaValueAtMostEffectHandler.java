package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepIfManaValueAtMostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
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
        CardEffect sacrificeIfLow = new SacrificePermanentsEffect(1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsSpecificPermanentPredicate(target.getId()),
                        new PermanentMaxManaValuePredicate(sacrifice.maxManaValue()))),
                SacrificeRecipient.CONTROLLER);
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                Triggers.endStep(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                entry.getSourcePermanentId(), target.getId(), sacrificeIfLow));
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " will be sacrificed at the beginning of the next end step if its mana value is "
                        + sacrifice.maxManaValue() + " or less."));
        log.info("Game {} - {} scheduled for conditional sacrifice at end step",
                gameData.id, target.getCard().getName());
    }
}
