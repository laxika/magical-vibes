package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher board effect (Tariff): in APNAP order, each player sacrifices the creature they control
 * with the greatest mana value unless they pay that creature's mana cost. When two or more of a
 * player's creatures are tied for greatest mana value, that player chooses which one is at risk.
 *
 * <p>Non-targeting SPELL-slot effect. Resolution is driven by
 * {@code EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffectHandler} and
 * {@code TariffSupport}: each affected player is prompted (via the may-ability system) to pay the
 * chosen creature's mana cost, and declining or being unable to pay sacrifices it.
 */
public record EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect() implements CardEffect {
}
