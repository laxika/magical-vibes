package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Gives the creature that paid a station cost keywords that remain with its card through zones. */
public record PerpetuallyGrantKeywordsToStationingCreatureEffect(Set<Keyword> keywords)
        implements CardEffect {
}
