package com.github.laxika.magicalvibes.carddata;

/** Controls when set oracle data is loaded into the card registry. */
public enum OracleLoadMode {
    /** Load every implemented set during registry startup. */
    EAGER,
    /** Load catalog requests and resolve missing card metadata during card construction. */
    ON_DEMAND,
    /** Load sets only through explicit {@link CardRegistry} requests. */
    EXPLICIT
}
