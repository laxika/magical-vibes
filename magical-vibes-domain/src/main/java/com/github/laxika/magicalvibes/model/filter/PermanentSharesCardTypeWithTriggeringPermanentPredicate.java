package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that share a card type with the permanent the triggered ability's event was about: the
 * permanent {@link FilterContext#triggeringPermanentId()} names while it is on the battlefield, judged by its
 * current types (Confusion in the Ranks' "shares a card type with it" for the permanent that entered), otherwise
 * {@link FilterContext#triggeringPermanentSnapshot()} as it last existed on the battlefield (Martyr's Bond's "each
 * opponent sacrifices a permanent that shares a card type with it").
 */
public record PermanentSharesCardTypeWithTriggeringPermanentPredicate() implements PermanentPredicate {
}
