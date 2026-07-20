package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Choose a card name, then search target player's graveyard, hand, and library for any number
 * of cards with that name and exile them. Then that player shuffles their library.
 *
 * <p>The offered names are narrowed two ways: {@code excludedTypes} drops names whose card has any
 * of those types (Memoricide / Cranial Extraction = nonland), and {@code requiredType} — when
 * non-null — keeps only names whose card has that type (Dispossess = "an artifact card name").
 *
 * <p>Used by: Memoricide, Cranial Extraction, Dispossess, etc.
 */
public record ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes, CardType requiredType) implements CardEffect {

    /** No required-type restriction (the offered names are only narrowed by {@code excludedTypes}). */
    public ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes) {
        this(excludedTypes, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
