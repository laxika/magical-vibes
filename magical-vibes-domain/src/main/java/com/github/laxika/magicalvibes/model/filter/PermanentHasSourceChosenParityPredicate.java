package com.github.laxika.magicalvibes.model.filter;

/**
 * "With mana value of the chosen quality" ({@code chosenQuality} true) or "without mana value of the chosen quality"
 * (false), the quality being the odd or even the source permanent chose as it entered
 * ({@code ChooseManaValueParityOnEnterEffect}; Ashling's Prerogative). Source-dependent: matches nothing, either way,
 * when the source is gone or made no choice. A face-down permanent's mana value is 0 (CR 708.2a).
 *
 * @param chosenQuality whether the permanent's mana value has the chosen quality (true) or the other one (false)
 */
public record PermanentHasSourceChosenParityPredicate(boolean chosenQuality) implements PermanentPredicate {
}
