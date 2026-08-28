package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static marker effect: "You may cast spells from among cards exiled with [this permanent]."
 * While a permanent with this effect is on the battlefield, the controller may cast cards tracked
 * for the source permanent, subject to the optional filter and restrictions below.
 *
 * @param anyManaType if true, mana of any type can be spent to cast the exiled spell
 *                    (e.g. Hostage Taker). If false, normal mana costs apply (e.g. Rona).
 * @param filter restricts which exiled cards may be cast, or {@code null} for any card
 * @param ownOnly if true, only cards owned by the source's controller may be cast
 * @param controllerTurnOnly if true, the permission applies only during the controller's turn
 * @param additionalCounterCost number of counters to remove from among creatures the controller
 *                              controls as an additional cost
 * @param manaValueLimit dynamic maximum mana value, or {@code null} for no limit
 * @param oncePerTurn whether this source can grant only one cast each turn
 * @param thisTurnOnly whether only cards exiled during the current turn qualify
 * @param withoutPayingManaCost whether qualifying spells are cast without paying their mana cost
 * @param stashCounterOnly whether this permission applies to cards with stash counters rather
 *                         than cards tracked with this source permanent
 * @param persistsAfterSourceLeaves whether matching cards retain an explicit play/cast permission
 *                                  after the source permanent leaves the battlefield
 * @param entryCounterType counter placed on a permanent cast with this permission, or {@code null}
 *                         when no entry counter is granted
 */
public record AllowCastFromCardsExiledWithSourceEffect(
        boolean anyManaType,
        CardPredicate filter,
        boolean ownOnly,
        boolean controllerTurnOnly,
        int additionalCounterCost,
        DynamicAmount manaValueLimit,
        boolean oncePerTurn,
        boolean thisTurnOnly,
        boolean withoutPayingManaCost,
        ExileAccessScope accessScope,
        boolean stashCounterOnly,
        boolean persistsAfterSourceLeaves,
        CounterType entryCounterType)
        implements CardEffect {

    public AllowCastFromCardsExiledWithSourceEffect(boolean anyManaType) {
        this(anyManaType, null, false, false, 0, null, false, false, false,
                ExileAccessScope.CONTROLLER, false, false, null);
    }

    public AllowCastFromCardsExiledWithSourceEffect(boolean anyManaType, ExileAccessScope accessScope) {
        this(anyManaType, null, false, false, 0, null, false, false, false, accessScope, false, false, null);
    }

    public AllowCastFromCardsExiledWithSourceEffect(boolean anyManaType, CardPredicate filter,
                                                     boolean ownOnly, boolean controllerTurnOnly,
                                                     int additionalCounterCost) {
        this(anyManaType, filter, ownOnly, controllerTurnOnly, additionalCounterCost,
                null, false, false, false, ExileAccessScope.CONTROLLER, false, false, null);
    }

    public AllowCastFromCardsExiledWithSourceEffect(boolean anyManaType, CardPredicate filter,
                                                     boolean ownOnly, boolean controllerTurnOnly,
                                                     int additionalCounterCost, DynamicAmount manaValueLimit,
                                                     boolean oncePerTurn, boolean thisTurnOnly,
                                                     boolean withoutPayingManaCost) {
        this(anyManaType, filter, ownOnly, controllerTurnOnly, additionalCounterCost,
                manaValueLimit, oncePerTurn, thisTurnOnly, withoutPayingManaCost,
                ExileAccessScope.CONTROLLER, false, false, null);
    }

    public AllowCastFromCardsExiledWithSourceEffect(boolean anyManaType, CardPredicate filter,
                                                     boolean ownOnly, boolean controllerTurnOnly,
                                                     int additionalCounterCost, DynamicAmount manaValueLimit,
                                                     boolean oncePerTurn, boolean thisTurnOnly,
                                                     boolean withoutPayingManaCost,
                                                     boolean persistsAfterSourceLeaves) {
        this(anyManaType, filter, ownOnly, controllerTurnOnly, additionalCounterCost,
                manaValueLimit, oncePerTurn, thisTurnOnly, withoutPayingManaCost,
                ExileAccessScope.CONTROLLER, false, persistsAfterSourceLeaves, null);
    }

    public static AllowCastFromCardsExiledWithSourceEffect forStashCounters(boolean anyManaType) {
        return new AllowCastFromCardsExiledWithSourceEffect(
                anyManaType, null, false, true, 0, null, false, false, false,
                ExileAccessScope.CONTROLLER, true, false, null);
    }

    /** Static source-linked permission that places {@code entryCounterType} on entered permanents. */
    public static AllowCastFromCardsExiledWithSourceEffect withEntryCounter(
            CardPredicate filter, CounterType entryCounterType) {
        return new AllowCastFromCardsExiledWithSourceEffect(
                false, filter, true, false, 0, null, false, false, false,
                ExileAccessScope.CONTROLLER, false, false, entryCounterType);
    }
}
