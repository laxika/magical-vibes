package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
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
                                      int convokeCreatureCount,
                                      EnterBattlefieldOnDiscardEffect discardReplacement,
                                      EnterWithCountersEffect enterWithCounters,
                                      Zone landPlayZone,
                                      StackEntry sourceStackEntry) {

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts, int convokeCreatureCount,
                                   EnterBattlefieldOnDiscardEffect discardReplacement,
                                   EnterWithCountersEffect enterWithCounters, Zone landPlayZone) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, convokeCreatureCount, discardReplacement, enterWithCounters,
                landPlayZone, null);
    }

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts, int convokeCreatureCount,
                                   EnterBattlefieldOnDiscardEffect discardReplacement,
                                   EnterWithCountersEffect enterWithCounters) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, convokeCreatureCount, discardReplacement, enterWithCounters, null, null);
    }

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts,
                                   EnterBattlefieldOnDiscardEffect discardReplacement,
                                   EnterWithCountersEffect enterWithCounters) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, 0, discardReplacement, enterWithCounters, null, null);
    }

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts,
                                   EnterBattlefieldOnDiscardEffect discardReplacement) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, 0, discardReplacement, null, null, null);
    }

    public BattlefieldEntryRequest(UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes,
                                   List<Permanent> simultaneouslyEntered, int xValue, boolean kicked,
                                   List<String> repeatedAdditionalCosts) {
        this(controllerId, permanent, enterTappedTypes, simultaneouslyEntered, xValue, kicked,
                repeatedAdditionalCosts, 0, null, null, null, null);
    }

    public BattlefieldEntryRequest {
        enterTappedTypes = Set.copyOf(enterTappedTypes);
        simultaneouslyEntered = List.copyOf(simultaneouslyEntered);
        repeatedAdditionalCosts = List.copyOf(repeatedAdditionalCosts);
    }

    public BattlefieldEntryRequest deepCopy() {
        return new BattlefieldEntryRequest(controllerId, new Permanent(permanent), enterTappedTypes,
                simultaneouslyEntered.stream().map(Permanent::new).toList(), xValue, kicked,
                repeatedAdditionalCosts, convokeCreatureCount, discardReplacement, enterWithCounters,
                landPlayZone, sourceStackEntry == null ? null : new StackEntry(sourceStackEntry));
    }
}
