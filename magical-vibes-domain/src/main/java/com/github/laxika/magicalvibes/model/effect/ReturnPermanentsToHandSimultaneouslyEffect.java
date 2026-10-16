package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses up to {@code count} distinct permanents they control matching {@code filter}
 * one at a time (fewer only when no more matching permanents are available), then all of the chosen
 * permanents are returned to their owners' hands at once. Every choice is made while the permanents
 * are still on the battlefield, so returning one cannot change which others qualify (Khalni Gem with
 * Ashaya, Soul of the Wild).
 *
 * @param filter which permanents may be chosen
 * @param noun   the singular noun used in prompts, e.g. "land"
 * @param count  how many permanents are chosen
 */
public record ReturnPermanentsToHandSimultaneouslyEffect(PermanentPredicate filter, String noun, int count)
        implements CardEffect {
}
