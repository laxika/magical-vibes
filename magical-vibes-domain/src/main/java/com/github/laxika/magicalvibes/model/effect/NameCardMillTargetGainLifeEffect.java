package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a card name, then target player mills a card. If a card with the chosen name was milled
 * this way, you gain life equal to its mana value." (Lammastide Weave)
 *
 * <p>On resolution the controller names a card, then the target player mills one card. If the milled
 * card's name matches the chosen name, the controller gains life equal to the milled card's mana
 * value. Any unconditional follow-up (e.g. "Draw a card.") is a separate effect on the spell.
 */
public record NameCardMillTargetGainLifeEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
