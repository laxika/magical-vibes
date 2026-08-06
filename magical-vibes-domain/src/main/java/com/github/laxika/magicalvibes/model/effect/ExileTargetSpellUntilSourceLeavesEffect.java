package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile target spell" where the exiled card stays tracked against the permanent whose ability
 * exiled it (Spell Queller's enter-the-battlefield trigger).
 *
 * <p>The spell leaves the stack without being countered, so "can't be countered" does not protect
 * it. The card is exiled with the source permanent recorded on its
 * {@link com.github.laxika.magicalvibes.model.ExiledCardEntry}, which is what
 * {@link MayCastCardsExiledWithSourceEffect} looks up when the source later leaves the
 * battlefield. A copy of a spell is not a card, so it ceases to exist instead of being exiled.</p>
 *
 * <p>Any restriction on which spells are legal targets (Spell Queller's "with mana value 4 or
 * less") lives on the card's {@code StackEntryPredicateTargetFilter}, matching Spellstutter
 * Sprite.</p>
 */
public record ExileTargetSpellUntilSourceLeavesEffect() implements CardEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.harmful(TargetPredicates.spellOnStack()); }
}
