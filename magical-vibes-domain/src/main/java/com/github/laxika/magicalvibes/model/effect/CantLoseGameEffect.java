package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller of this permanent can't lose the game. When
 * {@code opponentsCantWin} is set (the default), opponents of the controller also can't win the game
 * (Platinum Angel); a plain "you can't lose the game" (Liliana's Undead Minion) leaves it unset.
 */
public record CantLoseGameEffect(boolean opponentsCantWin) implements CardEffect {

    public CantLoseGameEffect() {
        this(true);
    }
}
