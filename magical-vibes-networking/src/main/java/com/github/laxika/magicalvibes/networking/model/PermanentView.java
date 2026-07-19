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
        List<ModifierLine> modifierLines,
        /** Face-up cards imprinted on / exiled with this permanent (Mimic Vat, Oblivion Ring,
         *  Myr Welder, ...), shown tucked under it on the battlefield. */
        List<CardView> exiledWithCards,
        /** Cards exiled face down with this permanent (hideaway lands, Grimoire Thief, ...);
         *  rendered as card backs so hidden information is not leaked. Zero in the controller's
         *  copy of the view — they receive the cards in {@link #faceDownExiledCards} instead. */
        int faceDownExiledCount,
        /** The face-down exiled cards themselves — populated only in the copy of the view sent
         *  to the permanent's controller (see {@link #withFaceDownRevealed}); empty for everyone
         *  else, who only get {@link #faceDownExiledCount} card backs. */
        List<CardView> faceDownExiledCards
) {
    /** Controller's copy of this view: the actual face-down cards replace the anonymous
     *  card-back count. */
    public PermanentView withFaceDownRevealed(List<CardView> cards) {
        return new PermanentView(id, card, tapped, attacking, blocking, blockingTargets, summoningSick,
                powerModifier, toughnessModifier, grantedKeywords, removedKeywords, effectivePower,
                effectiveToughness, attachedTo, chosenColor, chosenName, regenerationShield, cantBeBlocked,
                animatedCreature, counters, attackTargetId, markedDamage, transformed, prepared,
                modifierLines, exiledWithCards, 0, cards);
    }
}
