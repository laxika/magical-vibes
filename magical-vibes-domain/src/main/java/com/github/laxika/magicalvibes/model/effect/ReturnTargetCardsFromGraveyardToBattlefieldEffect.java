package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns targeted cards from the controller's graveyard to the battlefield.
 *
 * <p>The one-argument form returns exactly the spell's paid X cards. The fixed-cap form returns up
 * to {@code maxTargets} cards and can restrict them to cards put into the graveyard from the
 * battlefield this turn. It can also put counters on each returned permanent. The dynamic-cap form
 * is used by ETB abilities whose cap comes from the cast context, such as multikicker payments.</p>
 */
public record ReturnTargetCardsFromGraveyardToBattlefieldEffect(
        CardPredicate filter,
        int maxTargets,
        boolean fromBattlefieldThisTurn,
        boolean enterTapped,
        DynamicAmount dynamicMaxTargets,
        int maxTotalManaValue,
        CardColor grantColor,
        CardSubtype grantSubtype,
        CounterType counterType,
        int counterCount,
        GraveyardSearchScope source
) implements CardEffect {

    /** Creates the X-scaled form used by Return to the Ranks. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter) {
        this(filter, 0, false, false, null, 0, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /** Creates the fixed-cap form used by up-to-N reanimation spells. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, null, 0, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /** Creates a fixed-cap form that puts counters on each returned permanent. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              CounterType counterType, int counterCount) {
        this(filter, maxTargets, false, false, null, 0, null, null, counterType, counterCount,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /** Creates an ETB form whose up-to cap is evaluated from the entering spell's cast context. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter,
                                                              DynamicAmount dynamicMaxTargets) {
        this(filter, 0, false, false, dynamicMaxTargets, 0, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /** Creates an any-number form capped by the total mana value of the chosen cards. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect withinTotalManaValue(
            CardPredicate filter, int maxTotalManaValue) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, 0, false, false, null, maxTotalManaValue, null, null, null, 0,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /** Creates an any-number form that can return selected cards from any graveyard. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect withinTotalManaValueFromAllGraveyards(
            CardPredicate filter, int maxTotalManaValue) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, 0, false, false, null, maxTotalManaValue, null, null, null, 0,
                GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    /** Creates a fixed-cap form that also permanently grants a color and subtype. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped, CardColor grantColor,
                                                              CardSubtype grantSubtype) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, null, 0, grantColor, grantSubtype,
                null, 0, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
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
                maxTotalManaValue, grantColor, grantSubtype, counterType, counterCount,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
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
                                                              GraveyardSearchScope source) {
        if (maxTargets < 0) {
            throw new IllegalArgumentException("maxTargets cannot be negative");
        }
        if (maxTotalManaValue < 0) {
            throw new IllegalArgumentException("maxTotalManaValue cannot be negative");
        }
        this.filter = filter;
        this.maxTargets = maxTargets;
        this.fromBattlefieldThisTurn = fromBattlefieldThisTurn;
        this.enterTapped = enterTapped;
        this.dynamicMaxTargets = dynamicMaxTargets;
        this.maxTotalManaValue = maxTotalManaValue;
        this.grantColor = grantColor;
        this.grantSubtype = grantSubtype;
        this.counterType = counterType;
        this.counterCount = counterCount;
        this.source = source;
    }

    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect fromAllGraveyards(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, Integer.MAX_VALUE, false, false, null, 0, null, null, null, 0,
                GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    public boolean xScaled() {
        return maxTargets == 0 && dynamicMaxTargets == null && maxTotalManaValue == 0;
    }

    public boolean hasTotalManaValueCap() {
        return maxTotalManaValue > 0;
    }

    @Override
    public TargetSpec targetSpec() {
        return hasTotalManaValueCap()
                ? TargetSpec.benign(TargetPredicates.graveyardCards(filter, source))
                : TargetSpec.benign(TargetPredicates.graveyardCard(source));
    }
}
