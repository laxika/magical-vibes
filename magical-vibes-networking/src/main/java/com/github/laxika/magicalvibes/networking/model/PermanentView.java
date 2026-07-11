package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.layer.ModifierLine;

import java.util.List;
import java.util.Map;
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
        /** All counters on the permanent, keyed by counter type. Only present (non-zero) counters are included. */
        Map<CounterType, Integer> counters,
        UUID attackTargetId,
        int markedDamage,
        boolean transformed,
        /** Secrets of Strixhaven "Prepared": true while this permanent is prepared (a castable copy of
         *  its prepare spell sits in exile). Not a transform — the front face stays visible; the prepare
         *  spell is shown inset on the card. */
        boolean prepared,
        /** Per-source attribution of the continuous effects modifying this permanent, for the
         *  client's hover breakdown. Display-only; the aggregate fields above stay authoritative. */
        List<ModifierLine> modifierLines
) {}
