package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * "Choose a [nonland] card name": the controller chooses it as the effect resolves. The steps after it read it with
 * {@code CardHasNameChosenThisResolutionPredicate}; a source permanent also keeps it as its chosen name.
 *
 * @param requiredType      the type a card of the name must have, or {@code null}
 * @param excludedTypes     types whose names can't be chosen ("a nonland card name")
 * @param notBasicLandName  "a card name other than a basic land card name"
 */
public record ChooseCardNameAtResolutionEffect(CardType requiredType, List<CardType> excludedTypes,
                                               boolean notBasicLandName) implements CardEffect {

    public ChooseCardNameAtResolutionEffect {
        excludedTypes = List.copyOf(excludedTypes);
    }

    public ChooseCardNameAtResolutionEffect(CardType requiredType) {
        this(requiredType, List.of(), false);
    }

    public ChooseCardNameAtResolutionEffect() {
        this(null);
    }

    /** "Choose a nonland card name". */
    public static ChooseCardNameAtResolutionEffect excluding(CardType type) {
        return new ChooseCardNameAtResolutionEffect(null, List.of(type), false);
    }

    /** "Choose a card name other than a basic land card name". */
    public static ChooseCardNameAtResolutionEffect otherThanABasicLand() {
        return new ChooseCardNameAtResolutionEffect(null, List.of(), true);
    }
}
