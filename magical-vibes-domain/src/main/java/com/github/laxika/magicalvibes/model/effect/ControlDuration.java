package com.github.laxika.magicalvibes.model.effect;

/**
 * How long a {@link GainControlOfTargetEffect} keeps control of the stolen permanent.
 *
 * <p>Each resolution creates a floating {@code L2_CONTROL} continuous effect (CR 613.2/613.7,
 * see {@code GameData.floatingEffects}); the actual controller of a permanent is derived from
 * the newest active control effect. {@link #toDuration()} maps each value onto the
 * floating effect's {@link Duration}:
 *
 * <ul>
 *   <li>{@code PERMANENT} — indefinite control ({@code Duration.PERMANENT}).</li>
 *   <li>{@code END_OF_TURN} — the effect wears off during the cleanup step
 *       ({@code Duration.UNTIL_END_OF_TURN}).</li>
 *   <li>{@code UNTIL_END_OF_YOUR_NEXT_TURN} — the effect wears off during the cleanup step at
 *       the end of the controller's next turn ({@code Duration.UNTIL_END_OF_YOUR_NEXT_TURN}).</li>
 *   <li>{@code WHILE_SOURCE_ON_BATTLEFIELD} — the effect ends when the source permanent
 *       leaves the battlefield or its creator stops controlling it
 *       ({@code Duration.WHILE_SOURCE_ON_BATTLEFIELD}).</li>
 *   <li>{@code WHILE_SOURCE_REMAINS} — the effect ends only when the source permanent leaves
 *       the battlefield ({@code Duration.WHILE_SOURCE_REMAINS}); who controls the source
 *       does not matter (Infernal Denizen).</li>
 *   <li>{@code WHILE_SOURCE_TAPPED} — like {@code WHILE_SOURCE_ON_BATTLEFIELD}, but the effect
 *       additionally ends the moment the source becomes untapped and does not resume if it is
 *       tapped again (Seasinger — {@code Duration.WHILE_SOURCE_TAPPED}).</li>
 *   <li>{@code WHILE_SOURCE_REMAINS_TAPPED} — the effect ends when the source leaves the
 *       battlefield or becomes untapped, regardless of who controls the source (Vedalken
 *       Shackles).</li>
 *   <li>{@code UNTIL_END_OF_YOUR_NEXT_TURN} — the effect wears off during the cleanup step
 *       of the controller's next turn
 *       ({@code Duration.UNTIL_END_OF_YOUR_NEXT_TURN}).</li>
 * </ul>
 */
public enum ControlDuration {
    PERMANENT,
    END_OF_TURN,
    END_OF_COMBAT,
    UNTIL_END_OF_YOUR_NEXT_TURN,
    WHILE_SOURCE_ON_BATTLEFIELD,
    WHILE_SOURCE_REMAINS,
    WHILE_SOURCE_TAPPED,
    WHILE_SOURCE_REMAINS_TAPPED;

    /**
     * Whether control is tied to the source permanent, so the source must still be on the
     * battlefield (and, for source-linked durations other than {@code WHILE_SOURCE_REMAINS} and
     * {@code WHILE_SOURCE_REMAINS_TAPPED}, still be controlled by the effect's controller) for
     * control to be taken at all.
     */
    public boolean isSourceLinked() {
        return this == WHILE_SOURCE_ON_BATTLEFIELD || this == WHILE_SOURCE_REMAINS
                || this == WHILE_SOURCE_TAPPED || this == WHILE_SOURCE_REMAINS_TAPPED;
    }

    /** The {@link Duration} of the floating control effect this duration creates. */
    public Duration toDuration() {
        return switch (this) {
            case PERMANENT -> Duration.PERMANENT;
            case END_OF_TURN -> Duration.UNTIL_END_OF_TURN;
            case END_OF_COMBAT -> Duration.UNTIL_END_OF_COMBAT;
            case UNTIL_END_OF_YOUR_NEXT_TURN -> Duration.UNTIL_END_OF_YOUR_NEXT_TURN;
            case WHILE_SOURCE_ON_BATTLEFIELD -> Duration.WHILE_SOURCE_ON_BATTLEFIELD;
            case WHILE_SOURCE_REMAINS -> Duration.WHILE_SOURCE_REMAINS;
            case WHILE_SOURCE_TAPPED -> Duration.WHILE_SOURCE_TAPPED;
            case WHILE_SOURCE_REMAINS_TAPPED -> Duration.WHILE_SOURCE_REMAINS_TAPPED;
        };
    }
}
