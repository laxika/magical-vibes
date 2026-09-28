package com.github.laxika.magicalvibes.model.effect;

/** Each opponent chooses between redrawing their hand minus one and taking hand-size damage. */
public record EachOpponentFacesSycoraxCommanderVillainousChoiceEffect() implements CardEffect {

    public static final String DISCARD_OPTION =
            "That opponent discards all the cards in their hand, then draws that many cards minus one";
    public static final String DAMAGE_OPTION =
            "This creature deals damage to that player equal to the number of cards in their hand";
}
