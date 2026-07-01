package com.github.laxika.magicalvibes.service.battlefield.entertrigger;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Routes each enter-the-battlefield trigger effect to the {@link EnterTriggerHandler} that
 * claims it, or to a default "put the effect on the stack" fallback when none does. This is the
 * replacement for the {@code instanceof} cascades that used to live in the scan methods.
 */
@Component
public class EnterTriggerHandlerRegistry {

    private final List<EnterTriggerHandler> handlers;

    public EnterTriggerHandlerRegistry(List<EnterTriggerHandler> handlers) {
        this.handlers = handlers;
    }

    public void dispatch(EnterTriggerContext context, CardEffect effect) {
        for (EnterTriggerHandler handler : handlers) {
            if (handler.handledType().isInstance(effect)) {
                handler.handle(context, effect);
                return;
            }
        }
        // Default: put the effect straight onto the stack. The "any other creature enters" scan
        // only auto-queues non-targeting triggers, so skip targeting effects when it asks us to.
        if (context.isDefaultSkipsTargetingEffects() && isTargeting(effect)) {
            return;
        }
        context.enqueue(effect);
        context.logTriggered();
    }

    private static boolean isTargeting(CardEffect effect) {
        return effect.canTargetPlayer()
                || effect.canTargetPermanent()
                || effect.canTargetSpell()
                || effect.canTargetGraveyard()
                || effect.canTargetAnyGraveyard();
    }
}
