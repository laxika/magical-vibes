package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * The number of distinct keyword abilities from {@code abilities} found among the controller's
 * creatures. Each matching ability contributes once, regardless of how many creatures have it.
 */
public record DistinctKeywordAbilitiesAmongControlledCreatures(Set<Keyword> abilities)
        implements DynamicAmount {

    public DistinctKeywordAbilitiesAmongControlledCreatures {
        abilities = Set.copyOf(abilities);
    }
}
