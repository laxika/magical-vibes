package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.ArrayList;
import java.util.List;

/** Pregame choice of a keyword or ability word for Inspirational Antelope's Legacy ability. */
public record ChooseLegacyWordEffect(List<String> options) implements PregameChoiceEffect {

    private static final List<String> ABILITY_WORDS = List.of(
            "ADAMANT", "ALLIANCE", "BATTALION", "CHROMA", "COHORT", "CONSTELLATION",
            "CORRUPTED", "DELIRIUM", "DOMAIN", "ENRAGE", "FATEFUL HOUR", "FEROCIOUS",
            "FORMIDABLE", "GRANDEUR", "HELLBENT", "HEROIC", "INSPIRED", "KINSHIP",
            "LANDFALL", "LIEUTENANT", "METALCRAFT", "MORBID", "PACK TACTICS", "RADIANCE",
            "RAID", "RALLY", "REVOLT", "SPELL MASTERY", "STRIVE", "SURVIVAL", "THRESHOLD",
            "UNDERGROWTH", "VALIANT", "LEGACY");

    public ChooseLegacyWordEffect() {
        this(defaultOptions());
    }

    public ChooseLegacyWordEffect {
        options = List.copyOf(options);
    }

    private static List<String> defaultOptions() {
        List<String> options = new ArrayList<>();
        for (Keyword keyword : Keyword.values()) {
            options.add(keyword.name());
        }
        options.addAll(ABILITY_WORDS);
        return options.stream().distinct().toList();
    }
}
