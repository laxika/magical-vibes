package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;

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
                                      EnterBattlefieldOnDiscardEffect discardReplacement) {

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, null);
    }

    public BattlefieldEntryRequest {
        enterTappedTypes = Set.copyOf(enterTappedTypes);
        simultaneouslyEntered = List.copyOf(simultaneouslyEntered);
        repeatedAdditionalCosts = List.copyOf(repeatedAdditionalCosts);
    }
}
