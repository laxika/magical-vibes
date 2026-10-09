package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns targeted cards from the configured graveyard scope to the battlefield.
 *
 * <p>The one-argument form returns exactly the spell's paid X cards. The fixed-cap form returns up
 * to {@code maxTargets} cards, or at least {@code minTargets} when configured, and can restrict
 * them to cards put into the graveyard from the battlefield this turn. It can also put counters on
 * each returned permanent. The dynamic-cap form is used by ETB abilities whose cap comes from the
 * cast context, such as multikicker payments. The single-graveyard form can add haste and a delayed
 * sacrifice rider to the returned permanents.</p>
 */
public record ReturnTargetCardsFromGraveyardToBattlefieldEffect(
        CardPredicate filter,
        int maxTargets,
        boolean fromBattlefieldThisTurn,
        boolean enterTapped,
        DynamicAmount dynamicMaxTargets,
        int maxTotalManaValue,
        DynamicAmount dynamicMaxTotalManaValue,
        CardColor grantColor,
        CardSubtype grantSubtype,
        CounterType counterType,
        int counterCount,
        GraveyardSearchScope source,
        boolean singleGraveyard,
        boolean grantHaste,
        boolean sacrificeAtEndStep,
        int minTargets,
        int sacrificeAtEndStepIfManaValueAtLeast,
        boolean attachToSourceHost,
        boolean underOwnersControl,
        boolean randomlyReturnTwoAndPutRestOnBottom,
        AnimatePermanentsEffect entryAnimation,
        Condition entryAnimationCondition
) implements AggregateManaValueTargetEffect {

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
            boolean fromBattlefieldThisTurn, boolean enterTapped, DynamicAmount dynamicMaxTargets,
            int maxTotalManaValue, DynamicAmount dynamicMaxTotalManaValue, CardColor grantColor,
            CardSubtype grantSubtype, CounterType counterType, int counterCount, GraveyardSearchScope source,
            boolean singleGraveyard, boolean grantHaste, boolean sacrificeAtEndStep, int minTargets,
            int sacrificeAtEndStepIfManaValueAtLeast, boolean attachToSourceHost, boolean underOwnersControl,
            boolean randomlyReturnTwoAndPutRestOnBottom) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets, maxTotalManaValue,
                dynamicMaxTotalManaValue, grantColor, grantSubtype, counterType, counterCount, source,
                singleGraveyard, grantHaste, sacrificeAtEndStep, minTargets, sacrificeAtEndStepIfManaValueAtLeast,
                attachToSourceHost, underOwnersControl, randomlyReturnTwoAndPutRestOnBottom, null, null);
    }

    /** Animates only the permanents returned by this instruction, before they enter the battlefield. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect withEntryAnimation(
            AnimatePermanentsEffect animation, Condition condition) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(filter, maxTargets, fromBattlefieldThisTurn,
                enterTapped, dynamicMaxTargets, maxTotalManaValue, dynamicMaxTotalManaValue, grantColor,
                grantSubtype, counterType, counterCount, source, singleGraveyard, grantHaste, sacrificeAtEndStep,
                minTargets, sacrificeAtEndStepIfManaValueAtLeast, attachToSourceHost, underOwnersControl,
                randomlyReturnTwoAndPutRestOnBottom, animation, condition);
    }

    /** Creates the X-scaled form used by Return to the Ranks. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter) {
        this(filter, 0, false, false, null, 0, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false);
    }

    /** Creates the fixed-cap form used by up-to-N reanimation spells. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, null, 0, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false);
    }

    /** Creates a fixed-cap form that puts counters on each returned permanent. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              CounterType counterType, int counterCount) {
        this(filter, maxTargets, false, false, null, 0, null, null, counterType, counterCount,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false);
    }

    /** Creates a fixed-cap form that also restricts the chosen cards' total mana value. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              int maxTotalManaValue) {
        this(filter, maxTargets, false, false, null, maxTotalManaValue, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false);
    }

    /** Creates a dynamic-cap form whose up-to cap is evaluated from the spell's cast context. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter,
                                                              DynamicAmount dynamicMaxTargets) {
        this(filter, 0, false, false, dynamicMaxTargets, 0, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false);
    }

    /** Creates an any-number form capped by the total mana value of the chosen cards. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect withinTotalManaValue(
            CardPredicate filter, int maxTotalManaValue) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, 0, false, false, null, maxTotalManaValue, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false);
    }

    /** Creates an any-number form that can return selected cards from any graveyard. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect withinTotalManaValueFromAllGraveyards(
            CardPredicate filter, int maxTotalManaValue) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, 0, false, false, null, maxTotalManaValue, null, null, null, 0,
                GraveyardSearchScope.ALL_GRAVEYARDS, false, false, false);
    }

    /** Creates a fixed-cap form that also permanently grants a color and subtype. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped, CardColor grantColor,
                                                              CardSubtype grantSubtype) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, null, 0, grantColor, grantSubtype,
                null, 0, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets,
                maxTotalManaValue, null, grantColor, grantSubtype, counterType, counterCount,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false, 0, 0);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets,
                maxTotalManaValue, null, grantColor, grantSubtype, counterType, counterCount, source,
                singleGraveyard, grantHaste, sacrificeAtEndStep, 0, 0);
    }

    /** Creates a fixed-cap form with a required minimum number of targets. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect withTargetBounds(
            CardPredicate filter, int maxTargets, int minTargets) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(filter, maxTargets, false, false, null, 0, null, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false, minTargets, 0);
    }

    /** Creates Sinister Waltz's exact-three-target random return variant. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect sinisterWaltz(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, 3, false, false, null, 0, null, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false,
                3, 0, false, false, true);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep,
                                                              int minTargets) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets, maxTotalManaValue,
                null, grantColor, grantSubtype, counterType, counterCount, source, singleGraveyard,
                grantHaste, sacrificeAtEndStep, minTargets, 0);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              DynamicAmount dynamicMaxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep,
                                                              int minTargets,
                                                              int sacrificeAtEndStepIfManaValueAtLeast,
                                                              boolean attachToSourceHost,
                                                              boolean underOwnersControl) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets,
                maxTotalManaValue, dynamicMaxTotalManaValue, grantColor, grantSubtype,
                counterType, counterCount, source, singleGraveyard, grantHaste,
                sacrificeAtEndStep, minTargets, sacrificeAtEndStepIfManaValueAtLeast,
                attachToSourceHost, underOwnersControl, false);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              DynamicAmount dynamicMaxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep,
                                                              int minTargets,
                                                              int sacrificeAtEndStepIfManaValueAtLeast,
                                                              boolean attachToSourceHost,
                                                              boolean underOwnersControl,
                                                              boolean randomlyReturnTwoAndPutRestOnBottom,
                                                              AnimatePermanentsEffect entryAnimation,
                                                              Condition entryAnimationCondition) {
        if (maxTargets < 0) {
            throw new IllegalArgumentException("maxTargets cannot be negative");
        }
        if (minTargets < 0 || minTargets > maxTargets) {
            throw new IllegalArgumentException("minTargets must be between zero and maxTargets");
        }
        if (maxTotalManaValue < 0) {
            throw new IllegalArgumentException("maxTotalManaValue cannot be negative");
        }
        if (sacrificeAtEndStepIfManaValueAtLeast < 0) {
            throw new IllegalArgumentException("sacrificeAtEndStepIfManaValueAtLeast cannot be negative");
        }
        this.filter = filter;
        this.maxTargets = maxTargets;
        this.fromBattlefieldThisTurn = fromBattlefieldThisTurn;
        this.enterTapped = enterTapped;
        this.dynamicMaxTargets = dynamicMaxTargets;
        this.maxTotalManaValue = maxTotalManaValue;
        this.dynamicMaxTotalManaValue = dynamicMaxTotalManaValue;
        this.grantColor = grantColor;
        this.grantSubtype = grantSubtype;
        this.counterType = counterType;
        this.counterCount = counterCount;
        this.source = source;
        this.singleGraveyard = singleGraveyard;
        this.grantHaste = grantHaste;
        this.sacrificeAtEndStep = sacrificeAtEndStep;
        this.minTargets = minTargets;
        this.sacrificeAtEndStepIfManaValueAtLeast = sacrificeAtEndStepIfManaValueAtLeast;
        this.attachToSourceHost = attachToSourceHost;
        this.underOwnersControl = underOwnersControl;
        this.randomlyReturnTwoAndPutRestOnBottom = randomlyReturnTwoAndPutRestOnBottom;
        this.entryAnimation = entryAnimation;
        this.entryAnimationCondition = entryAnimationCondition;
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              DynamicAmount dynamicMaxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep,
                                                              int minTargets,
                                                              int sacrificeAtEndStepIfManaValueAtLeast,
                                                              boolean attachToSourceHost) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets,
                maxTotalManaValue, dynamicMaxTotalManaValue, grantColor, grantSubtype,
                counterType, counterCount, source, singleGraveyard, grantHaste,
                sacrificeAtEndStep, minTargets, sacrificeAtEndStepIfManaValueAtLeast,
                attachToSourceHost, false);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              DynamicAmount dynamicMaxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep,
                                                              int minTargets,
                                                              int sacrificeAtEndStepIfManaValueAtLeast) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets,
                maxTotalManaValue, dynamicMaxTotalManaValue, grantColor, grantSubtype,
                counterType, counterCount, source, singleGraveyard, grantHaste,
                sacrificeAtEndStep, minTargets, sacrificeAtEndStepIfManaValueAtLeast, false);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              DynamicAmount dynamicMaxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep,
                                                              int minTargets,
                                                              boolean attachToSourceHost) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets,
                maxTotalManaValue, dynamicMaxTotalManaValue, grantColor, grantSubtype,
                counterType, counterCount, source, singleGraveyard, grantHaste,
                sacrificeAtEndStep, minTargets, 0, attachToSourceHost);
    }

    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped,
                                                              DynamicAmount dynamicMaxTargets,
                                                              int maxTotalManaValue,
                                                              DynamicAmount dynamicMaxTotalManaValue,
                                                              CardColor grantColor,
                                                              CardSubtype grantSubtype,
                                                              CounterType counterType,
                                                              int counterCount,
                                                              GraveyardSearchScope source,
                                                              boolean singleGraveyard,
                                                              boolean grantHaste,
                                                              boolean sacrificeAtEndStep,
                                                              int minTargets) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, dynamicMaxTargets,
                maxTotalManaValue, dynamicMaxTotalManaValue, grantColor, grantSubtype,
                counterType, counterCount, source, singleGraveyard, grantHaste,
                sacrificeAtEndStep, minTargets, 0);
    }

    /** Returns any number of Auras and Equipment attached to the source Aura's host. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect anyNumberAttachedToSourceHost(
            CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, Integer.MAX_VALUE, false, false, null, 0, null, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, false, false, 0, true);
    }

    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect fromAllGraveyards(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, Integer.MAX_VALUE, false, false, null, 0, null, null, null, 0,
                GraveyardSearchScope.ALL_GRAVEYARDS, false, false, false);
    }

    /** Creates a bounded all-graveyards form that returns each card under its owner's control. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect fromAllGraveyardsUnderOwnersControl(
            CardPredicate filter, int maxTargets, boolean fromBattlefieldThisTurn, boolean enterTapped) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, maxTargets, fromBattlefieldThisTurn, enterTapped, null, 0, null, null, null, null, 0,
                GraveyardSearchScope.ALL_GRAVEYARDS, false, false, false, 0, 0, false, true);
    }

    /** Creates an all-graveyards form that puts counters on each returned permanent. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect fromAllGraveyards(
            CardPredicate filter, CounterType counterType, int counterCount) {
        return fromAllGraveyards(filter, Integer.MAX_VALUE, 0, counterType, counterCount);
    }

    /** Creates a bounded all-graveyards form that puts counters on each returned permanent. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect fromAllGraveyards(
            CardPredicate filter, int maxTargets, int minTargets,
            CounterType counterType, int counterCount) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, maxTargets, false, false, null, 0, null, null, null, counterType, counterCount,
                GraveyardSearchScope.ALL_GRAVEYARDS, false, false, false, minTargets, 0);
    }

    /** Creates a fixed-cap form targeting up to N cards from one available graveyard and returning them with riders. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect fromSingleGraveyard(
            CardPredicate filter, int maxTargets, boolean grantHaste, boolean sacrificeAtEndStep) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, maxTargets, false, false, null, 0, null, null, null, 0,
                GraveyardSearchScope.ALL_GRAVEYARDS, true, grantHaste, sacrificeAtEndStep);
    }

    /** Creates a mandatory single-card return from your graveyard with haste and a conditional end-step sacrifice. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect fromControllerGraveyardWithHasteAndConditionalSacrifice(
            CardPredicate filter, int minimumManaValue) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, 1, false, false, null, 0, null, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true, false, 1, minimumManaValue);
    }

    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect withinTotalManaValue(CardPredicate filter, DynamicAmount maxTotalManaValue, boolean grantHasteUntilEndOfTurn) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(filter, 0, false, false, maxTotalManaValue, 0, maxTotalManaValue, null, null, null, 0, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false, grantHasteUntilEndOfTurn, false, 0, 0);
    }

    public boolean grantHasteUntilEndOfTurn() {
        return grantHaste;
    }

    public boolean xScaled() {
        return maxTargets == 0 && dynamicMaxTargets == null && maxTotalManaValue == 0
                && dynamicMaxTotalManaValue == null;
    }

    public boolean hasTotalManaValueCap() {
        return maxTotalManaValue > 0 || dynamicMaxTotalManaValue != null;
    }

    @Override
    public boolean hasAggregateManaValueLimit() {
        return hasTotalManaValueCap();
    }

    @Override
    public TargetSpec targetSpec() {
        return hasTotalManaValueCap() || maxTargets > 1 || minTargets > 1
                ? TargetSpec.benign(TargetPredicates.graveyardCards(filter, source))
                : TargetSpec.benign(TargetPredicates.graveyardCard(source));
    }
}
