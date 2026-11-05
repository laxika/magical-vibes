package com.github.laxika.magicalvibes.model.effect;

/**
 * Bill Potts' "this ability triggers only once each turn", which covers its cast trigger and its activation trigger:
 * the engine marks the source permanent when either fires and skips the other until the turn ends. It is the one
 * ability of two trigger conditions whose effects are two event bindings (a copy of the spell, a copy of the ability),
 * so it can't be one {@code TriggeredAbility} with one {@code TriggerLimit} until an effect copies "that spell or
 * ability" from the trigger event (effect-slot-refactor.md step 15).
 * <p>
 * Don't author it on any other ability: "this ability triggers only once each turn" is
 * {@code TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)}, "do this only once each turn"
 * {@code TriggerLimit.DO_ONLY_ONCE_EACH_TURN}, and "for the first time each turn"
 * {@code TriggerLimit.FIRST_TIME_EACH_TURN}, which the collectors judge from the ability.
 */
public record OncePerTurnTriggerEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
