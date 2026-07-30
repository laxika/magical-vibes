package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: creatures controlled by the source permanent's controller that match
 * {@code filter} can't be blocked. Example: Tetsuko Umezawa, Fugitive — "Creatures you control
 * with power or toughness 1 or less can't be blocked."
 *
 * <p>This is a continuous effect that <strong>modifies the rules of the game</strong> (CR 613.11),
 * not one that modifies an object's characteristics: the wording grants the creatures no ability
 * (contrast "creatures you control gain 'this creature can't be blocked'"), it states a blocking
 * restriction, which CR 509.1b checks when blockers are declared. CR 613.11 applies such effects
 * <em>after all other continuous effects</em>, so the matching set is decided from fully layered
 * power and toughness — an anthem lifting a 1/1 to 2/2 takes the evasion away, and an opponent's
 * Cumber Stone dropping a 2/2 to 1/2 confers it. That is why this is its own effect type rather
 * than {@code GrantEffectEffect(CantBeBlockedEffect, …)}: a layer-6 ability grant would have its
 * scope filter evaluated during the layered pass, against numbers layer 7 has not produced yet.
 *
 * <p>Scoped to the source's own controller, mirroring
 * {@link ControlledCreaturesCantAttackUnlessPredicateEffect}. Like every other blocking and
 * attacking restriction in the model it carries no {@code LayerClassifier} entry and no static
 * handler — the block-legality query reads it straight off the battlefield.
 *
 * @param filter creatures matching this predicate can't be blocked
 */
public record ControlledCreaturesMatchingCantBeBlockedEffect(PermanentPredicate filter) implements CardEffect {
}
