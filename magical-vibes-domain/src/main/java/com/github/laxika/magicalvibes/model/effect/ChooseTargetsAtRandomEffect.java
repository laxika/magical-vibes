package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: while the source is on the battlefield with its abilities, the single target of
 * every spell and ability is chosen at random from its legal targets as it is put on the stack,
 * instead of by its controller (High Troller). Unlike {@link ReselectTargetAtRandomEffect}, this
 * does not use the stack.
 */
public record ChooseTargetsAtRandomEffect() implements CardEffect {
}
