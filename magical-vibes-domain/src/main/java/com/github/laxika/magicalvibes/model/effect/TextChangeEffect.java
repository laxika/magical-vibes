package com.github.laxika.magicalvibes.model.effect;

/**
 * A text change on a permanent: every instance of {@code fromWord} in its text reads {@code toWord} (CR 612.1), a
 * layer-3 effect (CR 613.1c). It is a floating continuous effect on that permanent; how long it lasts is the floating
 * effect's {@code Duration} ("until end of turn" for Whim of Volrath, none for Mind Bend and Magical Hack, CR 611.2a),
 * and the text changes on one permanent apply in timestamp order (CR 613.7), so a later one can rewrite a word an
 * earlier one introduced. {@code GameData.textReplacements} reads them; {@link ChangeColorTextEffect} makes them.
 */
public record TextChangeEffect(String fromWord, String toWord) implements CardEffect {
}
