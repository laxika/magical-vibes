package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all damage that would be dealt by instant and sorcery spells."
 * (Energy Storm). Global — any controller's permanent carrying this effect prevents all such
 * damage to any target. Does not affect combat damage, activated/triggered abilities, or damage
 * dealt by permanents that a spell merely causes to fight/bite. Queried by
 * {@code GameQueryService.isDamageFromInstantOrSorcerySpellPrevented} from {@code DamageSupport}.
 */
public record PreventDamageFromInstantAndSorcerySpellsEffect() implements CardEffect {
}
