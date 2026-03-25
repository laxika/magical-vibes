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
        Set<Keyword> removedKeywords,
        int effectivePower, int effectiveToughness,
        UUID attachedTo,
        CardColor chosenColor,
        String chosenName,
        int regenerationShield,
        boolean cantBeBlocked,
        boolean animatedCreature,
        int loyaltyCounters,
        int chargeCounters,
        int hatchlingCounters,
        int phylacteryCounters,
        int slimeCounters,
        int studyCounters,
        int wishCounters,
        int loreCounters,
        int aimCounters,
        int landmarkCounters,
        UUID attackTargetId,
        int markedDamage,
        boolean transformed
) {}
