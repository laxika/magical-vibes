package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PermanentView(
        UUID id, CardView card,
        boolean tapped, boolean attacking, boolean blocking,
        List<Integer> blockingTargets, boolean summoningSick,
        int powerModifier, int toughnessModifier,
        Set<Keyword> grantedKeywords,
        int effectivePower, int effectiveToughness,
        UUID attachedTo,
        CardColor chosenColor,
        int regenerationShield
) {}
