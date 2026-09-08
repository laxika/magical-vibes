package com.github.laxika.magicalvibes.model.effect;

/**
 * Ugin's Nexus replacement capabilities: skip extra turns while it remains on the battlefield,
 * and replace its own move from the battlefield to a graveyard with exile plus an extra turn.
 */
public record UginNexusReplacementEffect()
        implements ExtraTurnSkipReplacementEffect, ExileAndTakeExtraTurnReplacementEffect {
}
