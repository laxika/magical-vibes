package com.github.laxika.magicalvibes.model.effect;

/**
 * Inner-Flame Igniter's activated-ability resolution: if this is the third time the ability has
 * resolved this turn, creatures the controller controls gain first strike until end of turn.
 *
 * <p>The unconditional "+1/+0 until end of turn" half of the ability is a plain
 * {@link BoostAllOwnCreaturesEffect}; this effect handles only the conditional first-strike grant.
 * Counting is per source permanent and by resolutions (via
 * {@code GameData.permanentAbilityResolutionsThisTurn}, reset each turn), so the bonus fires only on
 * the exact third resolution and not on any later one — CR-consistent with the card's rulings.
 */
public record InnerFlameIgniterEffect() implements CardEffect {
}
