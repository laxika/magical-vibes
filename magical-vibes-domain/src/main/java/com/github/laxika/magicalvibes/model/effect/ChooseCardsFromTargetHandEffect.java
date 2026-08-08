package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Reveals the target player's hand and lets the caster choose cards for the specified destination.
 * The optional fields cover fallback discards, up-to choices, and same-name exile searches.
 */
public record ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                              List<CardType> includedTypes,
                                              HandChoiceDestination destination,
                                              boolean returnOnSourceLeave,
                                              CardPredicate filter,
                                              int declineFallbackDiscardCount,
                                              boolean upTo,
                                              boolean exileAllCopiesOfChosenNames)
        implements CombatDamageTriggerContextEffect {

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                0, false, false);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter, int declineFallbackDiscardCount) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                declineFallbackDiscardCount, false, false);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter, boolean upTo,
                                           boolean exileAllCopiesOfChosenNames) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                0, upTo, exileAllCopiesOfChosenNames);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, null);
    }

    /** "You may choose a card; if you don't, that player discards N cards." */
    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           HandChoiceDestination destination,
                                           int declineFallbackDiscardCount) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, null,
                declineFallbackDiscardCount);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           CardPredicate filter,
                                           HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, filter);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, includedTypes, destination, false, null);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave) {
        this(new Fixed(count), excludedTypes, includedTypes, destination,
                returnOnSourceLeave, null);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           HandChoiceDestination destination) {
        this(count, excludedTypes, List.of(), destination, false, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
