package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSourceUntapsOrLeavesTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates the delayed triggered ability of {@link RegisterDelayedSourceUntapsOrLeavesTriggerEffect} for the resolving
 * ability's target, its controller the ability's (CR 603.7e): "when [source] leaves the battlefield or becomes
 * untapped, [effect] on that creature", about the source and with no stated duration, so it triggers once, the next
 * time the source leaves or untaps (CR 603.7b). It is created while the source is on the battlefield, tapped or not:
 * an untapped source triggers it the next time it untaps (CR 603.7a). A source that has left the battlefield can
 * neither leave nor untap again, so nothing is created.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedSourceUntapsOrLeavesTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedSourceUntapsOrLeavesTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedSourceUntapsOrLeavesTriggerEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = entry.getTargetId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }
        CardEffect aimed = e.triggerEffect() instanceof RemoveAllCountersEffect removal
                ? removal.aimedAt(target.getId()) : e.triggerEffect();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(List.of(new TriggerCondition(Triggers.leavesBattlefield(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.becomesUntapped())), TriggerOptions.DEFAULT,
                        List.of(new EffectRegistration(aimed))),
                source.getId(), source.getId(), false));
        log.info("Game {} - {} creates an untaps-or-leaves delayed trigger for {}", gameData.id,
                entry.getCard().getName(), target.getCard().getName());
    }
}
