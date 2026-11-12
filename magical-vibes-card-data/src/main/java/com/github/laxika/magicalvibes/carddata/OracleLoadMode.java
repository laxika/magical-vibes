package com.github.laxika.magicalvibes.carddata;

/**
 * Controls when set oracle data is loaded into the card registry ({@code oracle.data-load-mode}).
 * In every mode a set is loaded and registered at most once per JVM.
 */
public enum OracleLoadMode {
    /** Load every implemented set during registry startup. The default, and what production uses. */
    EAGER,
    /**
     * Load catalog requests and resolve missing card metadata during card construction. The shared
     * card-test context uses this: a test's {@code @CardUsed} classes are preloaded through
     * {@link CardRegistry#ensureCardDataLoaded(java.util.List)}, and a card constructed without
     * its data has its set loaded by the resolver the registry installs on {@code Card}.
     */
    ON_DEMAND,
    /**
     * Load sets only through explicit {@link CardRegistry} requests. No resolver is installed, so
     * a card constructed before its set was requested has no metadata.
     */
    EXPLICIT
}
