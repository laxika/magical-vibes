package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library and lets them play it for as long as it
 * remains exiled.
 */
public record ExileTopCardMayPlayWhileExiledEffect(boolean faceDown, boolean anyManaType)
        implements CardEffect {

    public ExileTopCardMayPlayWhileExiledEffect() {
        this(false, false);
    }
}
