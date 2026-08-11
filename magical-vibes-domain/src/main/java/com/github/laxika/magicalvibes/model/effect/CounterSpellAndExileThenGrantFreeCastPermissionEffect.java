package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the target spell, exiles it instead of putting it into its owner's graveyard, and
 * grants this ability's controller permission to cast it without paying its mana cost for as long
 * as it remains exiled.
 */
public record CounterSpellAndExileThenGrantFreeCastPermissionEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
