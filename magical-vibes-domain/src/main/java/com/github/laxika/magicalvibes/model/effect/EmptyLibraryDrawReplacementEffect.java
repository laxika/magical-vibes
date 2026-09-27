package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static replacement that applies when its controller would draw from an empty
 * library. The draw service owns these replacements because they replace the draw event itself.
 */
public interface EmptyLibraryDrawReplacementEffect extends CardEffect {
}
