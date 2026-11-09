package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger: whenever any player taps a snow land for mana, that land doesn't untap
 * during its controller's next untap step, applied as Winter's Night's triggered mana ability resolves. Vorinclex,
 * Voice of Hunger's stack ability writes the same instruction as {@link SkipNextUntapEffect} with
 * {@code TapUntapScope.TRIGGERING} on {@code Triggers.opponentTapsLandForMana()}. Used by Winter's Night.
 *
 * <p>Increments {@code skipUntapCount} on the tapped land permanent. During the untap step,
 * the counter prevents untapping and is decremented.</p>
 */
public record TappedSnowLandDoesntUntapEffect() implements CardEffect {
}
