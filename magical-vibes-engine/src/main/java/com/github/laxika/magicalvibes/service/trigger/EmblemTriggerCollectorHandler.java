package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/** Handler for an effect stored on an emblem. */
@FunctionalInterface
public interface EmblemTriggerCollectorHandler {

    boolean handle(EmblemTriggerMatchContext match, CardEffect innerEffect, TriggerContext context);
}
