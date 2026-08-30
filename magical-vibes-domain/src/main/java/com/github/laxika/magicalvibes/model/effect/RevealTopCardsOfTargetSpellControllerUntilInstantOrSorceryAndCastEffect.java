package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the target spell controller's library until an instant or sorcery is found and offers
 * that card for casting without paying its mana cost. The revealed cards are shuffled back into
 * the library after the choice.
 */
public record RevealTopCardsOfTargetSpellControllerUntilInstantOrSorceryAndCastEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
