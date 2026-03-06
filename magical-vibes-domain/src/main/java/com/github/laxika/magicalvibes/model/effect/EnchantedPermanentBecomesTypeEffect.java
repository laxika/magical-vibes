package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static effect for auras that set the enchanted permanent's subtype.
 * "Enchanted [permanent] is a [subtype]" — replaces existing subtypes of the same category.
 *
 * <p>When the subtype is a basic land type (SWAMP, ISLAND, FOREST, MOUNTAIN, PLAINS),
 * per MTG rule 305.7 the land loses all other land types and abilities, gaining only
 * the intrinsic mana ability of the new type.
 *
 * <p>Reusable for cards like Evil Presence ("Enchanted land is a Swamp"),
 * Spreading Seas ("Enchanted land is an Island"), etc.
 *
 * @param subtype the subtype to set on the enchanted permanent
 */
public record EnchantedPermanentBecomesTypeEffect(CardSubtype subtype) implements CardEffect {

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = Set.of(
            CardSubtype.SWAMP, CardSubtype.ISLAND, CardSubtype.FOREST,
            CardSubtype.MOUNTAIN, CardSubtype.PLAINS
    );

    public boolean isBasicLandSubtype() {
        return BASIC_LAND_SUBTYPES.contains(subtype);
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
