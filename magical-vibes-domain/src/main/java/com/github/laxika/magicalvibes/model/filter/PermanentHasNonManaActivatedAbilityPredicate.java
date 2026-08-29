package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents that currently have at least one effective non-mana activated ability. */
public record PermanentHasNonManaActivatedAbilityPredicate(boolean levelUpOnly) implements PermanentPredicate {

    public PermanentHasNonManaActivatedAbilityPredicate() {
        this(false);
    }

    /** Matches permanents with an effective level-up ability. */
    public static PermanentHasNonManaActivatedAbilityPredicate levelUp() {
        return new PermanentHasNonManaActivatedAbilityPredicate(true);
    }
}
