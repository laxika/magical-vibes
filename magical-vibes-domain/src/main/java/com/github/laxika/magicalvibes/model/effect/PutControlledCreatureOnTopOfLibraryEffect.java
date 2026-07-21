package com.github.laxika.magicalvibes.model.effect;

/**
 * "Put a creature you control on top of its owner's library." (Nulltread Gargantuan.)
 *
 * <p>This is a mandatory, <em>non-targeted</em> choice made as the effect resolves (CR: the
 * ability doesn't target — the creature isn't chosen until resolution, so no one can respond to
 * the choice). The controller chooses one creature they control, which can be the source itself;
 * if it is the only creature they control they must put it on top of its owner's library. Handled
 * at resolution time via a {@code PutControlledCreatureOnTopOfLibrary} permanent choice.
 */
public record PutControlledCreatureOnTopOfLibraryEffect() implements CardEffect {
}
