package com.github.laxika.magicalvibes.model.effect;

/**
 * "Shuffle your library, then reveal the top card. If it's a nonland card, you may cast it without
 * paying its mana cost. If it's a land card, you may put it onto the battlefield and return
 * ~ to its owner's hand." (Unexpected Results)
 *
 * <p>The nonland branch is delegated to {@link RevealTopCardMayPlayFreeEffect} with
 * {@link LookDestination#TOP_OF_LIBRARY} — a card that isn't cast simply stays on top. The land
 * branch is handled by this effect's own may-handler because the land is <em>put</em> onto the
 * battlefield (it is not a land play, so it ignores the one-land-per-turn rule and sorcery timing)
 * and the source sorcery goes back to its owner's hand instead of the graveyard.</p>
 */
public record UnexpectedResultsEffect() implements CardEffect {
}
