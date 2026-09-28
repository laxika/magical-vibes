package com.github.laxika.magicalvibes.model.effect;

/**
 * Has an opponent choose one of the creature cards exiled as the activated ability's cost.
 * The chosen card goes to the bottom of its owner's library and the other returns tapped.
 */
public record OpponentChoosesOneOfActivatedExiledCreatureCardsEffect() implements CardEffect {
}
