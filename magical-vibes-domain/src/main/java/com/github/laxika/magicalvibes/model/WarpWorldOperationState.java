package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public class WarpWorldOperationState {

    public final Deque<WarpWorldAuraChoiceRequest> pendingAuraChoices = new ArrayDeque<>();
    public final List<WarpWorldEnchantmentPlacement> pendingEnchantmentPlacements = Collections.synchronizedList(new ArrayList<>());
    public final Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
    public boolean needsLegendChecks;
    public String sourceName;
}
