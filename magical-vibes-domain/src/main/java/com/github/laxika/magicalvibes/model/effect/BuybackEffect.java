package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect declaring that a spell has buyback — an optional additional mana cost
 * that can be paid when casting. (MTG Rule 702.27)
 *
 * <p>If the buyback cost is paid, the spell is put into its owner's hand as it resolves
 * instead of into its owner's graveyard. The card pairs this declaration with a
 * {@code ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell())} in the SPELL
 * slot; {@code SpellCastingService} pays the optional cost when the caster announces buyback
 * and stamps the stack entry so {@code BuybackPaid} reads it at resolution. A spell that is
 * countered or fizzles never resolves, so it still goes to the graveyard.
 *
 * @param cost the mana cost string for the buyback (e.g. "{3}")
 */
public record BuybackEffect(String cost) implements CardEffect {

    public boolean hasManaCost() {
        return cost != null && !cost.isEmpty();
    }
}
