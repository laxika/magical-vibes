package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Wrapper effect: "[body], then clash with an opponent. If you win, repeat this process."
 * (MTG rule 701.29). At resolution the {@code body} effects are dispatched in order for the
 * controller, then the controller clashes against their (2-player) opponent — both reveal the top
 * card of their library, the controller wins if their revealed card's mana value is strictly
 * greater. On a win the whole process (body, then clash) repeats; on a loss it stops.
 *
 * <p>Unlike {@link ClashEffect} — which clashes first and runs its reward only on a win — this
 * effect runs its {@code body} first and clashes afterwards, looping the entire sequence while the
 * controller keeps winning. Used by Hoarder's Greed (lose 2 life and draw two cards, then clash;
 * if you win, repeat).
 *
 * @param body the effects to execute at the start of each iteration, before the clash
 */
public record RepeatWhileWinningClashEffect(List<CardEffect> body) implements CardEffect {
}
