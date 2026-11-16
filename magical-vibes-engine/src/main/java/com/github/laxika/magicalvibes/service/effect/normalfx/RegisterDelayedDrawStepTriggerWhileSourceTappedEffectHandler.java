package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceRemainsTapped;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDrawStepTriggerWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the delayed triggered ability of {@link RegisterDelayedDrawStepTriggerWhileSourceTappedEffect} for the
 * resolving ability's target, its controller the ability's (CR 603.7e): "for as long as [source] remains tapped, at the
 * beginning of each of your draw steps, [effect] on that creature". It triggers at each of its controller's draw steps
 * and ends, without triggering, once the source has untapped or left the battlefield (CR 603.7b). A source that has
 * untapped or left the battlefield since the ability was activated has already ended the duration, so nothing is
 * created.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedDrawStepTriggerWhileSourceTappedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDrawStepTriggerWhileSourceTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedDrawStepTriggerWhileSourceTappedEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sourceSnapshot = entry.getSourcePermanentSnapshot();
        Permanent target = entry.getTargetId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || sourceSnapshot == null || target == null
                || source.getUntapSequence() != sourceSnapshot.getUntapSequence()) {
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atEveryStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.drawStep(ActivePlayer.YOU), e.triggerEffect()), source.getId(),
                target.getId(), DelayedReferents.permanents(List.of(target.getId())),
                new SourceRemainsTapped(source.getUntapSequence())));
        log.info("Game {} - {} creates a draw-step delayed trigger for {}", gameData.id,
                entry.getCard().getName(), target.getCard().getName());
    }
}
