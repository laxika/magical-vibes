package com.github.laxika.magicalvibes.carddata;

/**
 * Which upstream service the oracle registry is loaded from on startup, selected via the
 * {@code oracle.data-provider} application property. {@link #SCRYFALL} is the default when the
 * property is absent.
 *
 * <p>The selection is made by a {@code @ConditionalOnProperty} on each {@link OracleLoader}
 * matching one constant here by name, so these are the accepted property values. There is no
 * fallback between them: if the selected source fails, startup fails, because silently swapping
 * sources would let one run serve different oracle text than the next.
 *
 * <p>Nothing reads this enum at runtime — {@link OracleLoaderPresenceGuard} uses it to reject a
 * property value that matches no loader, which is what keeps it the single list of valid values.
 */
public enum OracleDataProvider {
    SCRYFALL,
    MTGJSON
}
