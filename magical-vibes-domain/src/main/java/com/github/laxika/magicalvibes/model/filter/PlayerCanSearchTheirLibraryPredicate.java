package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who could search their own library if the effect's controller had them do it: no "can't search
 * libraries" effect stops them (Stranglehold, an unpaid Leonin Arbiter). An opponent who can't search can't choose
 * "each opponent may search their library" (CR 608.2d); "each player who searched a library this way" after searches
 * that weren't a choice is the players who could (Tempt with Discovery).
 */
public record PlayerCanSearchTheirLibraryPredicate() implements PlayerPredicate {
}
