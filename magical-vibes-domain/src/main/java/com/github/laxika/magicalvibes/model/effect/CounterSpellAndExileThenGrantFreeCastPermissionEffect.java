package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the target spell, exiles it instead of putting it into its owner's graveyard, and
 * grants this ability's controller permission to cast it without paying its mana cost for as long
 * as it remains exiled. The optional permanent-spell mode sends other spells to the graveyard
 * without granting a casting permission. {@code mayCastNow} instead offers the controller a
 * "you may cast it without paying its mana cost" choice right away, leaving no permission behind
 * if declined (Transcendent Dragon).
 */
public record CounterSpellAndExileThenGrantFreeCastPermissionEffect(boolean permanentSpellsOnly,
                                                                    boolean mayCastNow)
        implements CounterSpellingEffect {

    public CounterSpellAndExileThenGrantFreeCastPermissionEffect() {
        this(false, false);
    }

    public CounterSpellAndExileThenGrantFreeCastPermissionEffect(boolean permanentSpellsOnly) {
        this(permanentSpellsOnly, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
