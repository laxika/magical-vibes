package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarpWorldOperationState {

    public final Deque<WarpWorldAuraChoiceRequest> pendingAuraChoices = new ArrayDeque<>();
    public final List<WarpWorldEnchantmentPlacement> pendingEnchantmentPlacements = Collections.synchronizedList(new ArrayList<>());
    public final Map<UUID, List<Card>> pendingCreaturesByPlayer = new ConcurrentHashMap<>();
    public boolean needsLegendChecks;
    public String sourceName;
}
