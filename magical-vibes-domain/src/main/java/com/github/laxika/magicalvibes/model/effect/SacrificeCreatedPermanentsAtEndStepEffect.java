package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule every permanent created earlier in this same resolution to be sacrificed at the beginning
 * of the next end step. Place it in the same slot right after a token-creating effect for the
 * "Create a … token. Sacrifice it at the beginning of the next end step." wording (Tidal Wave).
 * With {@code controllerEndStepOnly} the sacrifice waits for the resolving controller's next end step
 * and skips opponents' end steps ("At the beginning of your next end step" - Foggy Swamp Visions).
 *
 * <p>Reads {@code StackEntry.createdPermanentIds}, so it only ever touches the tokens this
 * resolution made — the same mechanism {@code DestroyAllPermanentsEffect.sparingPermanentsCreatedThisResolution}
 * and {@code GrantScope.CREATED_THIS_RESOLUTION} use. Sacrifice, not destruction (ignores
 * indestructible and regeneration).
 */
public record SacrificeCreatedPermanentsAtEndStepEffect(boolean controllerEndStepOnly) implements CardEffect {

    public SacrificeCreatedPermanentsAtEndStepEffect() {
        this(false);
    }
}
