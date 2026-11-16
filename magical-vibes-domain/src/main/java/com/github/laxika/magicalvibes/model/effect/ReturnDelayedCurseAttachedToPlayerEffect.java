package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution of a delayed "return that Curse to the battlefield attached to you at the beginning of the next end
 * step" ability (Lynde, Cheerful Tormentor): it returns the Curse card its entry names as its triggering card to the
 * battlefield, under its owner's control, attached to the ability's controller, if the card is still in the
 * graveyard stay it was in when the ability was made (CR 400.7).
 */
public record ReturnDelayedCurseAttachedToPlayerEffect() implements CardEffect {
}
