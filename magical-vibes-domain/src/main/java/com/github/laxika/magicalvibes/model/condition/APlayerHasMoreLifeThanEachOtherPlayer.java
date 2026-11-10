package com.github.laxika.magicalvibes.model.condition;

/**
 * A player has more life than each other player: "if a player has more life than each other player" (Ghazbán Ogre,
 * Wild Dogs).
 */
public record APlayerHasMoreLifeThanEachOtherPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "a player has more life than each other player";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player has more life than each other player";
    }
}
