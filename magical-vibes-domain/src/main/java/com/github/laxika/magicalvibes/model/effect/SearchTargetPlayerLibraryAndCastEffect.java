package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Search target opponent's library for a card of one of the given types, you may cast that card
 * without paying its mana cost, then that player shuffles their library (e.g. Knowledge
 * Exploitation searches for an instant or sorcery card). Targets a player.
 */
public record SearchTargetPlayerLibraryAndCastEffect(Set<CardType> castableTypes) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
