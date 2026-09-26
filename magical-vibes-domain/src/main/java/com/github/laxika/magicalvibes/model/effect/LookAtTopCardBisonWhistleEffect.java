package com.github.laxika.magicalvibes.model.effect;

/**
 * Bison Whistle's private top-card look: a Bison may enter the battlefield, a creature may go to
 * hand, and any other card may go to the graveyard.
 */
public record LookAtTopCardBisonWhistleEffect(Stage stage) implements CardEffect {

    public enum Stage {
        LOOK,
        MAY_BATTLEFIELD
    }

    public LookAtTopCardBisonWhistleEffect() {
        this(Stage.LOOK);
    }

    public LookAtTopCardBisonWhistleEffect withStage(Stage nextStage) {
        return new LookAtTopCardBisonWhistleEffect(nextStage);
    }
}
