package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Choose a card name, then search target player's graveyard, hand, and library for cards with that
 * name and exile up to {@code maxCount} of them. Then that player shuffles their library.
 *
 * <p>The offered names are narrowed two ways: {@code excludedTypes} drops names whose card has any
 * of those types (Memoricide / Cranial Extraction = nonland), and {@code requiredType} — when
 * non-null — keeps only names whose card has that type (Dispossess = "an artifact card name").
 *
 * <p>When {@code maxCount} is {@link Integer#MAX_VALUE}, the selection is unlimited. The optional
 * draw flag supports effects that make the target draw once for each card exiled from their hand.
 * <p>Used by: Memoricide, Cranial Extraction, Dispossess, Lost Legacy, The Stone Brain, etc.
 */
public record ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes, CardType requiredType,
                                                     int maxCount, boolean drawForHandExiled,
                                                     boolean excludeBasicLandNames,
                                                     CreateTokenEffect tokenTemplate)
        implements CardEffect {

    public ChooseCardNameAndExileFromZonesEffect {
        excludedTypes = List.copyOf(excludedTypes);
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }

    /** Unlimited selection with no draw rider. */
    public ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes, CardType requiredType) {
        this(excludedTypes, requiredType, Integer.MAX_VALUE, false, false, null);
    }

    public ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes, CardType requiredType,
                                                  boolean drawForHandExiled) {
        this(excludedTypes, requiredType, Integer.MAX_VALUE, drawForHandExiled, false, null);
    }

    public ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes, CardType requiredType,
                                                  int maxCount, boolean drawForHandExiled) {
        this(excludedTypes, requiredType, maxCount, drawForHandExiled, false, null);
    }

    public ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes, CardType requiredType,
                                                  boolean drawForHandExiled, int maxCount) {
        this(excludedTypes, requiredType, maxCount, drawForHandExiled, false, null);
    }

    public ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes, CardType requiredType,
                                                  boolean excludeBasicLandNames,
                                                  CreateTokenEffect tokenTemplate) {
        this(excludedTypes, requiredType, Integer.MAX_VALUE, false, excludeBasicLandNames, tokenTemplate);
    }

    /** No required-type restriction (the offered names are only narrowed by {@code excludedTypes}). */
    public ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes) {
        this(excludedTypes, null, Integer.MAX_VALUE, false, false, null);
    }

    /** No draw follow-up; retained for the existing name-choice exile cards. */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
