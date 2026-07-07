package com.github.laxika.magicalvibes.model.effect;

/**
 * How long a {@link GainControlOfTargetEffect} keeps control of the stolen permanent.
 *
 * <ul>
 *   <li>{@code PERMANENT} — indefinite control (tracked via
 *       {@code GameData.permanentControlStolenCreatures}).</li>
 *   <li>{@code END_OF_TURN} — control reverts during the end-of-turn cleanup
 *       (tracked via {@code GameData.untilEndOfTurnStolenCreatures}).</li>
 *   <li>{@code WHILE_SOURCE_ON_BATTLEFIELD} — control lasts for as long as the
 *       controller keeps the source permanent; it reverts when the source leaves
 *       or changes controllers (tracked via {@code GameData.sourceDependentStolenCreatures}).</li>
 * </ul>
 */
public enum ControlDuration {
    PERMANENT,
    END_OF_TURN,
    WHILE_SOURCE_ON_BATTLEFIELD
}
