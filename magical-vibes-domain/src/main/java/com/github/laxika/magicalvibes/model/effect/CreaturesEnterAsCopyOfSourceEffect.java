package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "Creatures you control enter as a copy of this creature."
 * When any creature would enter the battlefield under the controller of the permanent
 * bearing this effect, it enters as a copy of that permanent instead (MTG Rule 614.1c).
 */
public record CreaturesEnterAsCopyOfSourceEffect() implements CardEffect {
}
