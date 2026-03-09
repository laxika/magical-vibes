package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger: whenever an opponent taps a land for mana, that land doesn't untap
 * during its controller's next untap step. Used by Vorinclex, Voice of Hunger.
 *
 * <p>Increments {@code skipUntapCount} on the tapped land permanent. During the untap step,
 * the counter prevents untapping and is decremented.</p>
 */
public record OpponentTappedLandDoesntUntapEffect() implements CardEffect {
}
