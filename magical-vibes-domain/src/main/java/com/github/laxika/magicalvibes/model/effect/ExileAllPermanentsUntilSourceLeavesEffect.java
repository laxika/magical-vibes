package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exile every permanent matching {@code filter} across all battlefields until the source
 * permanent leaves the battlefield. When the source leaves (by any means), each exiled card
 * returns to the battlefield under its owner's control, tapped iff {@code returnTapped}.
 *
 * <p>The return linkage reuses {@link com.github.laxika.magicalvibes.model.GameData#exileReturnOnPermanentLeave},
 * which now holds one entry per exiled card. Used by Realm Razer (exile all lands, return tapped).
 *
 * @param filter       which permanents to exile
 * @param returnTapped whether the returned permanents enter tapped
 */
public record ExileAllPermanentsUntilSourceLeavesEffect(PermanentPredicate filter, boolean returnTapped)
        implements CardEffect {
}
