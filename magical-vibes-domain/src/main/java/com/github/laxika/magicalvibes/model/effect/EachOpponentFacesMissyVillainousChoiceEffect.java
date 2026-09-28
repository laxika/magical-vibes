package com.github.laxika.magicalvibes.model.effect;

/** Missy's opponent-by-opponent choice between artifact damage and drawing plus chaos. */
public record EachOpponentFacesMissyVillainousChoiceEffect() implements CardEffect {

    public static final String DAMAGE_OPTION =
            "Each artifact creature you control deals 1 damage to you";
    public static final String DRAW_OPTION = "You draw a card and chaos ensues";
}
