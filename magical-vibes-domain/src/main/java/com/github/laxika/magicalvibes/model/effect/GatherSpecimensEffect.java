package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * A turn-scoped control-changing replacement (CR 614.1) floated on its controller until end of turn:
 * "if a creature would enter the battlefield under an opponent's control this turn, it enters under your
 * control instead" (Gather Specimens, {@link Kind#CREATURES_ENTERING}) or "each token that would be created
 * under an opponent's control this turn is created under your control instead" (Crafty Cutpurse,
 * {@link Kind#TOKENS_CREATED}). Each resolved effect applies at most once to an event (CR 614.5); read by
 * {@code GameQueryService.resolveGatherSpecimensController} and {@code resolveTokenCreationController}.
 *
 * @param kind    the event it replaces
 */
public record GatherSpecimensEffect(Kind kind) implements CardEffect {

    /** The event whose controller is changed. */
    public enum Kind {
        /** A creature entering the battlefield, a creature token included. */
        CREATURES_ENTERING,
        /** A token being created, of any type. */
        TOKENS_CREATED
    }

    public GatherSpecimensEffect {
        Objects.requireNonNull(kind, "kind");
    }

    /** Gather Specimens: creatures entering under an opponent's control. */
    public GatherSpecimensEffect() {
        this(Kind.CREATURES_ENTERING);
    }

    /** Crafty Cutpurse: tokens created under an opponent's control. */
    public static GatherSpecimensEffect tokensCreated() {
        return new GatherSpecimensEffect(Kind.TOKENS_CREATED);
    }
}
