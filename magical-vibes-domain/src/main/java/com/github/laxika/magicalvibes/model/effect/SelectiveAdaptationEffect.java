package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import java.util.List;

/** Reveals cards and selects at most one card for each listed keyword. */
public record SelectiveAdaptationEffect(int count, List<Keyword> keywords) implements CardEffect {

    public SelectiveAdaptationEffect {
        count = Math.max(0, count);
        keywords = List.copyOf(keywords);
    }
}
