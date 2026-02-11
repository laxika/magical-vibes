package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PermanentView(
        UUID id, Card card,
        boolean tapped, boolean attacking, boolean blocking,
        List<Integer> blockingTargets, boolean summoningSick,
        int powerModifier, int toughnessModifier,
        Set<Keyword> grantedKeywords,
        int effectivePower, int effectiveToughness
) {}
