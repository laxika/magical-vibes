package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect that notes the most prevalent creature type in a library as the source
 * permanent enters the battlefield.
 *
 * @param opponentLibrary whether to inspect an opponent's library; otherwise the controller's
 *                       library is inspected
 */
public record NoteMostPrevalentCreatureTypeOnEnterEffect(boolean opponentLibrary)
        implements ReplacementEffect {

    public NoteMostPrevalentCreatureTypeOnEnterEffect() {
        this(true);
    }
}
