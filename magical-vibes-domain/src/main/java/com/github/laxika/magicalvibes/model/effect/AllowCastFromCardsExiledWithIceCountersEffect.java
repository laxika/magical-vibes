package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission: the controller may cast nonland cards in exile that an opponent owns and
 * that have ice counters on them. The permission is independent of which permanent put the card
 * into exile, so it also sees cards exiled by previous Draugr Necromancers.
 *
 * @param anyManaType whether snow-produced mana may be spent as though it were mana of any color
 */
public record AllowCastFromCardsExiledWithIceCountersEffect(boolean anyManaType)
        implements CardEffect {
}
