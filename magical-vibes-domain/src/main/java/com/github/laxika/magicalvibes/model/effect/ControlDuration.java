package com.github.laxika.magicalvibes.model.effect;

/**
 * How long a {@link GainControlOfTargetEffect} keeps control of the stolen permanent.
 *
 * <p>Each resolution creates a floating {@code L2_CONTROL} continuous effect (CR 613.2/613.7,
 * see {@code GameData.floatingEffects}); the actual controller of a permanent is derived from
 * the newest active control effect. {@link #toEffectDuration()} maps each value onto the
 * floating effect's {@link EffectDuration}:
 *
 * <ul>
 *   <li>{@code PERMANENT} — indefinite control ({@code EffectDuration.PERMANENT}).</li>
 *   <li>{@code END_OF_TURN} — the effect wears off during the cleanup step
 *       ({@code EffectDuration.UNTIL_END_OF_TURN}).</li>
 *   <li>{@code WHILE_SOURCE_ON_BATTLEFIELD} — the effect ends when the source permanent
 *       leaves the battlefield or its creator stops controlling it
 *       ({@code EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD}).</li>
 *   <li>{@code WHILE_SOURCE_REMAINS} — the effect ends only when the source permanent leaves
 *       the battlefield ({@code EffectDuration.WHILE_SOURCE_REMAINS}); who controls the source
 *       does not matter (Infernal Denizen).</li>
 *   <li>{@code WHILE_SOURCE_TAPPED} — like {@code WHILE_SOURCE_ON_BATTLEFIELD}, but the effect
 *       additionally ends the moment the source becomes untapped and does not resume if it is
 *       tapped again (Seasinger — {@code EffectDuration.WHILE_SOURCE_TAPPED}).</li>
 * </ul>
 */
public enum ControlDuration {
    PERMANENT,
    END_OF_TURN,
    WHILE_SOURCE_ON_BATTLEFIELD,
    WHILE_SOURCE_REMAINS,
    WHILE_SOURCE_TAPPED;

    /** The {@link EffectDuration} of the floating control effect this duration creates. */
    public EffectDuration toEffectDuration() {
        return switch (this) {
            case PERMANENT -> EffectDuration.PERMANENT;
            case END_OF_TURN -> EffectDuration.UNTIL_END_OF_TURN;
            case WHILE_SOURCE_ON_BATTLEFIELD -> EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD;
            case WHILE_SOURCE_REMAINS -> EffectDuration.WHILE_SOURCE_REMAINS;
            case WHILE_SOURCE_TAPPED -> EffectDuration.WHILE_SOURCE_TAPPED;
        };
    }
}
