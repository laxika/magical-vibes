package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Arrays;
import java.util.List;

/**
 * The alternative material cost of a craft ability. The default form is the original craft
 * wording: exile another artifact permanent controlled by the activating player or an artifact
 * card in that player's graveyard.
 */
public record CraftMaterialCost(int minimumCount, CardType requiredType, CardSubtype requiredSubtype,
                                List<CardSubtype> requiredSubtypes, boolean nonlandOnly,
                                boolean requireActivatedAbility, boolean allowsAdditionalMaterials) implements CostEffect {

    public CraftMaterialCost() {
        this(1, CardType.ARTIFACT, null, List.of(), false, false, false);
    }

    public CraftMaterialCost(int minimumCount, CardType requiredType, boolean nonlandOnly,
                             boolean requireActivatedAbility) {
        this(minimumCount, requiredType, null, List.of(), nonlandOnly, requireActivatedAbility, false);
    }

    public CraftMaterialCost(int minimumCount, CardType requiredType, CardSubtype requiredSubtype,
                             boolean nonlandOnly, boolean requireActivatedAbility) {
        this(minimumCount, requiredType, requiredSubtype, List.of(), nonlandOnly,
                requireActivatedAbility, false);
    }

    public CraftMaterialCost(CardSubtype requiredSubtype) {
        this(1, null, requiredSubtype, List.of(), false, false, false);
    }

    public CraftMaterialCost(List<CardSubtype> requiredSubtypes) {
        this(requiredSubtypes.size(), null, null, requiredSubtypes, false, false, false);
    }

    public static CraftMaterialCost withRequiredSubtypes(CardSubtype... requiredSubtypes) {
        return new CraftMaterialCost(Arrays.asList(requiredSubtypes));
    }

    public CraftMaterialCost {
        requiredSubtypes = requiredSubtypes == null ? List.of() : List.copyOf(requiredSubtypes);
        if (minimumCount < 1) {
            throw new IllegalArgumentException("Craft material count must be positive");
        }
        if (requiredType != null && (requiredSubtype != null || !requiredSubtypes.isEmpty())) {
            throw new IllegalArgumentException("Craft material cannot require both a card type and subtype");
        }
        if (requiredSubtype != null && !requiredSubtypes.isEmpty()) {
            throw new IllegalArgumentException("Craft material cannot require multiple subtype forms");
        }
        if (!requiredSubtypes.isEmpty() && minimumCount != requiredSubtypes.size()) {
            throw new IllegalArgumentException("Craft material count must equal the number of required subtypes");
        }
    }

    /** The material form used by The Enigma Jewel. */
    public static CraftMaterialCost nonlandsWithActivatedAbilities(int minimumCount) {
        return new CraftMaterialCost(minimumCount, null, null, List.of(), true, true, true);
    }

    public static CraftMaterialCost oneOrMore() {
        return new CraftMaterialCost(1, null, null, List.of(), false, false, true);
    }

    public static CraftMaterialCost oneOrMore(CardSubtype requiredSubtype) {
        return new CraftMaterialCost(1, null, requiredSubtype, List.of(), false, false, true);
    }
}
