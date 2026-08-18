package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "You may cast a spell from among cards exiled with this permanent without paying its mana cost."
 * (Shell of the Last Kappa's sacrifice ability.) An optional {@code manaValue} restricts the
 * offered spells to cards with exactly that mana value.
 *
 * <p>The <em>ability's controller</em> — not the exiled card's owner — is offered the cast, and
 * only one of the exiled cards may be cast. Contrast {@link MayCastCardsExiledWithSourceEffect},
 * which offers <em>every</em> card exiled with a departing permanent to its own owner (Spell
 * Queller).</p>
 */
public record MayCastCardExiledWithSourceEffect(DynamicAmount manaValue) implements CardEffect {

    public MayCastCardExiledWithSourceEffect() {
        this(null);
    }
}
