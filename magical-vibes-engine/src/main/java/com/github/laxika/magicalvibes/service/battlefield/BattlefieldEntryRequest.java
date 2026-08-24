package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable inputs needed while determining how a permanent enters the battlefield.
 */
public record BattlefieldEntryRequest(UUID controllerId,
                                      Permanent permanent,
                                      Set<CardType> enterTappedTypes,
                                      List<Permanent> simultaneouslyEntered,
                                      int xValue,
                                      boolean kicked,
                                      List<String> repeatedAdditionalCosts,
                                      EnterBattlefieldOnDiscardEffect discardReplacement,
                                      EnterWithCountersEffect enterWithCounters) {

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts,
                                   EnterBattlefieldOnDiscardEffect discardReplacement) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, discardReplacement, null);
    }

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, null, null);
    }

    public BattlefieldEntryRequest {
        enterTappedTypes = Set.copyOf(enterTappedTypes);
        simultaneouslyEntered = List.copyOf(simultaneouslyEntered);
        repeatedAdditionalCosts = List.copyOf(repeatedAdditionalCosts);
    }
}
