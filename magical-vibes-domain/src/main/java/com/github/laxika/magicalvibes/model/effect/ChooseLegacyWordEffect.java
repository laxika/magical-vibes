package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacyWordSupport;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Pregame choice of a keyword or ability word for Inspirational Antelope's Legacy ability. */
public record ChooseLegacyWordEffect(List<String> options) implements PregameChoiceEffect {

    private static final List<String> ABILITY_WORDS = List.of(
            "ADAMANT", "ALLIANCE", "BATTALION", "CHROMA", "COHORT", "CONSTELLATION",
            "CORRUPTED", "DELIRIUM", "DOMAIN", "ENRAGE", "FATEFUL HOUR", "FEROCIOUS",
            "FORMIDABLE", "GRANDEUR", "HELLBENT", "HEROIC", "INSPIRED", "KINSHIP",
            "LANDFALL", "LIEUTENANT", "METALCRAFT", "MORBID", "PACK TACTICS", "RADIANCE",
            "RAID", "RALLY", "REVOLT", "SPELL MASTERY", "STRIVE", "SURVIVAL", "THRESHOLD",
            "UNDERGROWTH", "VALIANT", "LEGACY");

    /** Keyword actions share the keyword enum but are not keywords or ability words. */
    private static final Set<Keyword> KEYWORD_ACTIONS = EnumSet.of(
            Keyword.SCRY, Keyword.FIGHT, Keyword.EXPLORE, Keyword.TRANSFORM, Keyword.MELD, Keyword.ADAPT,
            Keyword.AMASS, Keyword.ASSEMBLE, Keyword.SUSPECT, Keyword.DOUBLE, Keyword.WATERBEND,
            Keyword.BLIGHT, Keyword.HEAL, Keyword.RECRUIT);

    /** Keyword abilities that no card carries as a {@link Keyword}. */
    private static final List<String> EXTRA_KEYWORDS = List.of(LegacyWordSupport.CYCLING);

    public ChooseLegacyWordEffect() {
        this(defaultOptions());
    }

    public ChooseLegacyWordEffect {
        options = List.copyOf(options);
    }

    private static List<String> defaultOptions() {
        List<String> options = new ArrayList<>();
        for (Keyword keyword : Keyword.values()) {
            if (!KEYWORD_ACTIONS.contains(keyword)) {
                options.add(keyword.name());
            }
        }
        options.addAll(EXTRA_KEYWORDS);
        options.addAll(ABILITY_WORDS);
        return options.stream().distinct().toList();
    }
}
