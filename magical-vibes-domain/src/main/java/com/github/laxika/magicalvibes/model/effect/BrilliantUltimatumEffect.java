package com.github.laxika.magicalvibes.model.effect;

/**
 * Brilliant Ultimatum: exile the top {@code count} cards of your library. An opponent separates
 * those cards into two piles. You may play lands and cast spells from one of those piles; spells
 * cast this way are cast without paying their mana costs.
 *
 * <p>Flow (see {@code BrilliantUltimatumEffectHandler} / {@code BrilliantUltimatumSupport}):
 * <ol>
 *   <li>On resolution: exile the top {@code count} cards of the controller's library.</li>
 *   <li>An opponent separates the exiled cards into two piles (reuses the card-pile separation
 *       flow via {@link com.github.laxika.magicalvibes.model.PendingPileSeparation} with
 *       {@code playFromExile = true}).</li>
 *   <li>Controller chooses a pile (may-ability: Yes = Pile 1, No = Pile 2).</li>
 *   <li>Controller may play lands (respecting the one-land-per-turn limit) and cast spells for
 *       free from the chosen pile. Everything not played remains exiled.</li>
 * </ol>
 */
public record BrilliantUltimatumEffect(int count) implements CardEffect {
}
