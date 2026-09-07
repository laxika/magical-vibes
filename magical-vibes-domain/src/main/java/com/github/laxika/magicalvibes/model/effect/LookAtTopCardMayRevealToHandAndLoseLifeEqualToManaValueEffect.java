package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top card of the controller's library. The controller may reveal that card and put
 * it into their hand; if they do, they lose life equal to its mana value.
 */
public record LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect(Stage stage)
        implements CardEffect {

    public enum Stage {
        LOOK,
        MAY_REVEAL
    }

    public LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect() {
        this(Stage.LOOK);
    }

    public LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect withMayRevealStage() {
        return new LookAtTopCardMayRevealToHandAndLoseLifeEqualToManaValueEffect(Stage.MAY_REVEAL);
    }
}
