package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for an Aura card that could enchant the creature recorded as
 * the resolving stack entry's (non-targeting) target, puts it onto the battlefield attached to that
 * creature, then shuffles. The host is the trigger's recorded creature (e.g. the lone attacker for
 * an "attacks alone" trigger), not chosen by the searcher. Wrap in {@link MayEffect} for "you may".
 * When {@code restrictToTriggeringAura} is true, the search also uses the triggering Aura's mana
 * value as an upper bound and excludes names of Auras already controlled by the searcher.
 * Used by Sovereigns of Lost Alara and Light-Paws, Emperor's Voice.
 *
 * @param restrictToTriggeringAura whether to apply Light-Paws's mana-value and name restrictions
 */
public record SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect(
        boolean restrictToTriggeringAura) implements CardEffect {

    public SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect() {
        this(false);
    }
}
