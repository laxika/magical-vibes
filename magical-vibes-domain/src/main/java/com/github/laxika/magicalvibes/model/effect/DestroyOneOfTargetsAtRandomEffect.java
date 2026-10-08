package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys one permanent at random from the list of targets stored in
 * {@code StackEntry.targetIds}.
 *
 * <p>Used by Capricious Efreet's upkeep trigger: "choose target nonland
 * permanent you control and up to two target nonland permanents you don't
 * control. Destroy one of them at random."
 */
public record DestroyOneOfTargetsAtRandomEffect() implements CardEffect {

    /** The controller restriction belongs to each declared target position. */
    public static com.github.laxika.magicalvibes.model.filter.TargetFilter targetFilter(boolean controlled) {
        var nonland = new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(
                new com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate());
        if (controlled) {
            return new com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter(
                    nonland, "Choose a nonland permanent you control.");
        }
        return new com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter(
                new com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate(java.util.List.of(
                        nonland, new com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate(
                                new com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate()))),
                "Choose a nonland permanent you don't control.");
    }
}
