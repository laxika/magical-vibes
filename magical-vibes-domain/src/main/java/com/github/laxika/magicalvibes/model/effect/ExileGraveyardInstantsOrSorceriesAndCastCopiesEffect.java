package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile target instant or sorcery card from a graveyard. Copy that card. Cast the copy if able
 * without paying its mana cost." — applied to every target of the spell (Spelltwine exiles one card
 * from each of two graveyards, so its two target groups are declared with
 * {@code GraveyardCardPredicateTargetFilter}s and this effect stays unbound to read both).
 *
 * <p>Each original is exiled, a copy of it is created in exile, and the copies are cast for free as
 * copies — so they cease to exist on resolution rather than being put into a graveyard.</p>
 */
public record ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyGraveyardCard());
    }
}
