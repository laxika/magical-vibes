package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker stored in an Emblem's staticEffects list: "Whenever you cast a creature spell, you may
 * search your library for a creature card, put it onto the battlefield, then shuffle." Recognised
 * as a capability interface in {@code TriggerCollectionService} (so the concrete marker is not an
 * effect-dispatch instanceof).
 *
 * <p>Garruk, Caller of Beasts' emblem. The trigger fires on the cast, so it resolves before the
 * creature spell itself; the search is optional ("you may") and can fail to find.
 */
public interface SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect extends CardEffect {

    /** Concrete instance placed on the emblem. */
    record Marker() implements SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect {
    }
}
