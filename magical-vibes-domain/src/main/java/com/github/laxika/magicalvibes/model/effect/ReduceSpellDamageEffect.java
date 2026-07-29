package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a spell would deal damage to a permanent or player, it deals that
 * much damage minus N to that permanent or player instead." (Benevolent Unicorn)
 *
 * <p>Global — any controller's permanent carrying this effect reduces the damage of every spell, no
 * matter who controls it. Applies to damage dealt by the spell itself (all spell types), not to
 * combat damage, activated/triggered abilities, or damage dealt by a permanent that a spell merely
 * causes to fight/bite. Being a replacement effect rather than prevention, it still applies while
 * damage can't be prevented. Multiple instances stack. Queried by
 * {@code GameQueryService.getSpellDamageReduction} from {@code DamageSupport}.
 */
public record ReduceSpellDamageEffect(int amount) implements CardEffect {
}
