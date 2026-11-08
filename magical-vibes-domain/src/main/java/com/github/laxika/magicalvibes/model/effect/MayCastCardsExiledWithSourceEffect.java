package com.github.laxika.magicalvibes.model.effect;

/**
 * "When this permanent leaves the battlefield, the exiled card's owner may cast that card without
 * paying its mana cost." (Spell Queller)
 *
 * <p>Pairs with {@link ExileTargetSpellUntilSourceLeavesEffect}: the cards exiled by the source are
 * found through the {@code sourcePermanentId} recorded on their
 * {@link com.github.laxika.magicalvibes.model.ExiledCardEntry}, the stack entry's source permanent, which the
 * leaves-the-battlefield ability's entry keeps after the source is gone. The offer goes to the exiled card's
 * <em>owner</em>, not to this permanent's controller, and is routed through the
 * {@link MayPlayExiledCardWithoutPayingManaCostEffect} marker. A declined card stays exiled.</p>
 */
public record MayCastCardsExiledWithSourceEffect() implements CardEffect {
}
