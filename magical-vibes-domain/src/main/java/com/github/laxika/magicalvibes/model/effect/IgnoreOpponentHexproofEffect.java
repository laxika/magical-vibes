package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[Your opponents and] [creatures / permanents] your opponents control with hexproof can be the targets of spells
 * and abilities you control as though they didn't have hexproof": Kaya, Bane of the Dead (opponents, permanents),
 * Glaring Spotlight and Nowhere to Run (creatures), Detection Tower (opponents, creatures, floated "until end of turn"
 * on its controller by {@link GrantStaticEffectToPlayerEffect}). Only hexproof is lifted, a "hexproof from [quality]"
 * too, as it is a hexproof ability (CR 702.11d); shroud and protection still apply.
 *
 * <p>As a static ability it is its controller's; floated, it is the player's it is on. Read by
 * {@code GameQueryService.ignoresOpponentPlayerHexproof} / {@code ignoresOpponentPermanentHexproof} /
 * {@code ignoresOpponentCreatureHexproof}.
 *
 * @param opponents  whether the opponents themselves can be targeted as though they didn't have hexproof
 * @param permanents which of the opponents' permanents it covers
 */
public record IgnoreOpponentHexproofEffect(boolean opponents, Breadth permanents) implements CardEffect {

    /** Which of the opponents' permanents an {@link IgnoreOpponentHexproofEffect} covers. */
    public enum Breadth {
        CREATURES,
        PERMANENTS
    }

    /** "Your opponents and permanents your opponents control with hexproof" (Kaya, Bane of the Dead). */
    public IgnoreOpponentHexproofEffect() {
        this(true, Breadth.PERMANENTS);
    }

    public IgnoreOpponentHexproofEffect {
        Objects.requireNonNull(permanents, "permanents");
    }

    /** "Creatures your opponents control with hexproof." */
    public static IgnoreOpponentHexproofEffect opponentsCreatures() {
        return new IgnoreOpponentHexproofEffect(false, Breadth.CREATURES);
    }

    /** "Your opponents and creatures your opponents control with hexproof." */
    public static IgnoreOpponentHexproofEffect opponentsAndTheirCreatures() {
        return new IgnoreOpponentHexproofEffect(true, Breadth.CREATURES);
    }
}
