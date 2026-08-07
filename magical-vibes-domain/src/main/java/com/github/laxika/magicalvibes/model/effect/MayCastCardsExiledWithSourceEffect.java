package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "When this permanent leaves the battlefield, the exiled card's owner may cast that card without
 * paying its mana cost." (Spell Queller)
 *
 * <p>Pairs with {@link ExileTargetSpellUntilSourceLeavesEffect}: the cards exiled by the source are
 * found through the {@code sourcePermanentId} recorded on their
 * {@link com.github.laxika.magicalvibes.model.ExiledCardEntry}. The offer goes to the exiled card's
 * <em>owner</em>, not to this permanent's controller, and is routed through the
 * {@link MayPlayExiledCardWithoutPayingManaCostEffect} marker. A declined card stays exiled.</p>
 *
 * <p>The source has already left the battlefield when the trigger resolves, so its permanent id is
 * baked into the effect by the trigger collector; the card-level instance leaves it {@code null}.</p>
 */
public record MayCastCardsExiledWithSourceEffect(UUID sourcePermanentId) implements CardEffect {

    public MayCastCardsExiledWithSourceEffect() {
        this(null);
    }
}
