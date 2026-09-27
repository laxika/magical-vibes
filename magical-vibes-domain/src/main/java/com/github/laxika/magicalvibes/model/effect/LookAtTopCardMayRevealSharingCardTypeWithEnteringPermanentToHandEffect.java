package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top card of the controller's library. If it shares a card type with the permanent
 * that caused the trigger, the controller may reveal it and put it into their hand.
 */
public record LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect(
        Stage stage
) implements CardEffect {

    public LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect() {
        this(Stage.LOOK);
    }

    public LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect withStage(Stage stage) {
        return new LookAtTopCardMayRevealSharingCardTypeWithEnteringPermanentToHandEffect(stage);
    }

    public enum Stage {
        LOOK,
        MAY_HAND
    }
}
