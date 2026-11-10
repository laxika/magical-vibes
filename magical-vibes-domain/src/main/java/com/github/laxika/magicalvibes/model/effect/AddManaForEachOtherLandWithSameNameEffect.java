package com.github.laxika.magicalvibes.model.effect;

/**
 * Sasaya's Essence's "add an additional one mana of any type that land produced for each other land you control with
 * the same name as it": the lands are counted as the triggered mana ability resolves, among the lands the entry's
 * triggering player (who tapped the land) controls other than the tapped land, by the tapped land's name as it is or
 * as it last existed; each mana is of the type the land produced (the stack entry's produced mana colors), chosen
 * one at a time when it produced several.
 */
public record AddManaForEachOtherLandWithSameNameEffect() implements TriggeredManaAbilityEffect {

    @Override
    public boolean readsProducedMana() {
        return true;
    }
}
