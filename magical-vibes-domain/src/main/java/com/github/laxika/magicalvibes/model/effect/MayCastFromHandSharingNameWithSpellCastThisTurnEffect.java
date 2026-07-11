package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may cast a spell from your hand without paying its mana cost if it has the same name as a
 * spell that was cast this turn." (Twinning Glass)
 *
 * <p>On resolution the controller is offered each eligible hand card (a nonland card whose name
 * matches a spell any player cast this turn — CR: the caster doesn't matter) as a
 * may-cast-from-hand-without-paying choice via {@link MayCastFromHandWithoutPayingManaCostEffect};
 * casting one clears the rest, so only a single spell is cast.
 */
public record MayCastFromHandSharingNameWithSpellCastThisTurnEffect() implements CardEffect {
}
