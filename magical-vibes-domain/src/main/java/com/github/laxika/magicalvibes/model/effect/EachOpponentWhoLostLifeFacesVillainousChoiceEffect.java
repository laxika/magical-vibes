package com.github.laxika.magicalvibes.model.effect;

/** Each qualifying opponent chooses between the controller drawing and discarding a card. */
public record EachOpponentWhoLostLifeFacesVillainousChoiceEffect(int minimumLifeLost)
        implements CardEffect {
}
