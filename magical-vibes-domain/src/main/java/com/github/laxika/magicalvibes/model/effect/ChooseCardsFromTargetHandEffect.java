package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Lets the caster inspect the target player's hand and choose cards for the specified destination.
 * The optional fields cover public reveals, fallback discards, up-to choices, and same-name exile
 * searches.
 */
public record ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                              List<CardType> includedTypes,
                                              HandChoiceDestination destination,
                                              boolean returnOnSourceLeave,
                                              CardPredicate filter,
                                              int declineFallbackDiscardCount,
                                              boolean upTo,
                                              boolean exileAllCopiesOfChosenNames,
                                              boolean imprintOnSource,
                                              boolean revealHand,
                                              boolean grantPlayPermission,
                                              boolean returnAtNextEndStep,
                                              int exilePlayOpponentTax,
                                              CardEffect declineEffect)
        implements CombatDamageTriggerContextEffect {

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter,
                                           int declineFallbackDiscardCount,
                                           boolean upTo,
                                           boolean exileAllCopiesOfChosenNames,
                                           boolean imprintOnSource,
                                           boolean revealHand,
                                           boolean grantPlayPermission,
                                           boolean returnAtNextEndStep,
                                           int exilePlayOpponentTax) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                declineFallbackDiscardCount, upTo, exileAllCopiesOfChosenNames, imprintOnSource,
                revealHand, grantPlayPermission, returnAtNextEndStep, exilePlayOpponentTax, null);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter,
                                           int declineFallbackDiscardCount,
                                           boolean upTo,
                                           boolean exileAllCopiesOfChosenNames,
                                           boolean imprintOnSource,
                                           boolean revealHand,
                                           boolean grantPlayPermission,
                                           boolean returnAtNextEndStep) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                declineFallbackDiscardCount, upTo, exileAllCopiesOfChosenNames, imprintOnSource,
                revealHand, grantPlayPermission, returnAtNextEndStep, 0);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter,
                                           int declineFallbackDiscardCount,
                                           boolean upTo,
                                           boolean exileAllCopiesOfChosenNames,
                                           boolean imprintOnSource,
                                           boolean revealHand) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                declineFallbackDiscardCount, upTo, exileAllCopiesOfChosenNames, imprintOnSource,
                revealHand, false, false);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                0, false, false, false, true, false, false);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter, int declineFallbackDiscardCount) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                declineFallbackDiscardCount, false, false, false, true, false, false);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter, boolean upTo,
                                           boolean exileAllCopiesOfChosenNames) {
        this(count, excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                0, upTo, exileAllCopiesOfChosenNames, false, true, false, false);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, null);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           boolean returnOnSourceLeave,
                                           CardPredicate filter,
                                           boolean grantPlayPermission,
                                           boolean returnAtNextEndStep) {
        this(new Fixed(count), excludedTypes, includedTypes, destination, returnOnSourceLeave, filter,
                0, false, false, false, true, grantPlayPermission, returnAtNextEndStep);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           HandChoiceDestination destination, boolean imprintOnSource) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, null,
                0, false, false, imprintOnSource, true, false, false);
    }

    public static ChooseCardsFromTargetHandEffect exileAndGrantPlayPermission(
            int count, List<CardType> excludedTypes, int exilePlayOpponentTax) {
        return new ChooseCardsFromTargetHandEffect(new Fixed(count), excludedTypes, List.of(),
                HandChoiceDestination.EXILE, false, null,
                0, true, false, false, true, true, false, exilePlayOpponentTax);
    }

    /** "You may choose a card; if you don't, that player discards N cards." */
    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes,
                                           HandChoiceDestination destination,
                                           int declineFallbackDiscardCount) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, null,
                declineFallbackDiscardCount);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> includedTypes,
                                           HandChoiceDestination destination,
                                           CardEffect declineEffect) {
        this(new Fixed(count), List.of(), includedTypes, destination, false, null,
                0, true, false, false, true, false, false, 0, declineEffect);
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

    public static ChooseCardsFromTargetHandEffect lookAtTargetHand(
            DynamicAmount count, HandChoiceDestination destination) {
        return new ChooseCardsFromTargetHandEffect(count, List.of(), List.of(), destination,
                false, null, 0, false, false, false, false, false, false);
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
