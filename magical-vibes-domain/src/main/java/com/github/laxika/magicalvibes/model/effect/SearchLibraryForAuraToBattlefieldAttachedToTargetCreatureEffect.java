package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for an Aura card that could enchant the creature recorded as
 * the resolving stack entry's (non-targeting) target, puts it onto the battlefield attached to that
 * creature, then shuffles. The host is the trigger's recorded creature (e.g. the lone attacker for
 * an "attacks alone" trigger), not chosen by the searcher. Wrap in {@link MayEffect} for "you may".
 * Used by Sovereigns of Lost Alara.
 */
public record SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect() implements CardEffect {
}
