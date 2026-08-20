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
        int counterCount
) implements CardEffect {

    /** Creates the X-scaled form used by Return to the Ranks. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter) {
        this(filter, 0, false, false, null, 0, null, null, null, 0);
    }

    /** Creates the fixed-cap form used by up-to-N reanimation spells. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, null, 0, null, null, null, 0);
    }

    /** Creates a fixed-cap form that puts counters on each returned permanent. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              CounterType counterType, int counterCount) {
        this(filter, maxTargets, false, false, null, 0, null, null, counterType, counterCount);
    }

    /** Creates an ETB form whose up-to cap is evaluated from the entering spell's cast context. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter,
                                                              DynamicAmount dynamicMaxTargets) {
        this(filter, 0, false, false, dynamicMaxTargets, 0, null, null, null, 0);
    }

    /** Creates an any-number form capped by the total mana value of the chosen cards. */
    public static ReturnTargetCardsFromGraveyardToBattlefieldEffect withinTotalManaValue(
            CardPredicate filter, int maxTotalManaValue) {
        return new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                filter, 0, false, false, null, maxTotalManaValue, null, null, null, 0);
    }

    /** Creates a fixed-cap form that also permanently grants a color and subtype. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped, CardColor grantColor,
                                                              CardSubtype grantSubtype) {
        this(filter, maxTargets, fromBattlefieldThisTurn, enterTapped, null, 0, grantColor, grantSubtype,
                null, 0);
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
                ? TargetSpec.benign(TargetPredicates.graveyardCards(filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                : TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
