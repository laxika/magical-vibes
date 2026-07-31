package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Static effect for auras that set the enchanted permanent's subtype(s).
 * "Enchanted [permanent] is a [subtype]" — or several basic land types — replaces existing
 * subtypes of the same category.
 *
 * <p>When every subtype is a basic land type (SWAMP, ISLAND, FOREST, MOUNTAIN, PLAINS),
 * per MTG rule 305.7 the land loses all other land types and abilities, gaining the
 * intrinsic mana ability of each new type (tap for one mana of a corresponding color).
 *
 * <p>Reusable for cards like Evil Presence ("Enchanted land is a Swamp"),
 * Sea's Claim ("Enchanted land is an Island"), and Lush Growth
 * ("Enchanted land is a Mountain, Forest, and Plains").
 *
 * @param subtypes the subtype(s) to set on the enchanted permanent (non-empty)
 */
public record EnchantedPermanentBecomesTypeEffect(List<CardSubtype> subtypes) implements CardEffect {

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = Set.of(
            CardSubtype.SWAMP, CardSubtype.ISLAND, CardSubtype.FOREST,
            CardSubtype.MOUNTAIN, CardSubtype.PLAINS
    );

    public EnchantedPermanentBecomesTypeEffect {
        Objects.requireNonNull(subtypes, "subtypes");
        if (subtypes.isEmpty()) {
            throw new IllegalArgumentException("subtypes must not be empty");
        }
        subtypes = List.copyOf(subtypes);
    }

    /** Single-type convenience (Evil Presence, Sea's Claim, …). */
    public EnchantedPermanentBecomesTypeEffect(CardSubtype subtype) {
        this(List.of(Objects.requireNonNull(subtype, "subtype")));
    }

    /** First (or only) subtype — kept for text-change / single-type call sites. */
    public CardSubtype subtype() {
        return subtypes.getFirst();
    }

    public boolean isBasicLandSubtype() {
        return BASIC_LAND_SUBTYPES.containsAll(subtypes);
    }

    /**
     * Maps a basic land subtype to its intrinsic mana color.
     */
    public static ManaColor manaColorForLandSubtype(CardSubtype subtype) {
        return switch (subtype) {
            case SWAMP -> ManaColor.BLACK;
            case ISLAND -> ManaColor.BLUE;
            case FOREST -> ManaColor.GREEN;
            case MOUNTAIN -> ManaColor.RED;
            case PLAINS -> ManaColor.WHITE;
            default -> throw new IllegalArgumentException("Not a basic land subtype: " + subtype);
        };
    }
}
