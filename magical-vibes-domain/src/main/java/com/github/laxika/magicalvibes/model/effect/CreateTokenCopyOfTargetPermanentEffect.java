package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates a token that's a copy of the permanent referenced by the stack entry's targetId.
 * Used for "create a token that's a copy of that artifact/creature" triggered abilities
 * where the permanent to copy is determined at trigger time (e.g. Mirrorworks).
 * The token copies all copiable characteristics per CR 707.2.
 *
 * <p>Optional overrides support "except it's X/Y", "except it's also a [type]",
 * additional subtypes, and counters placed after the token enters.
 */
public record CreateTokenCopyOfTargetPermanentEffect(
        List<CardSubtype> additionalSubtypes,
        Set<CardType> additionalTypes,
        Integer powerOverride,
        Integer toughnessOverride,
        Map<CounterType, Integer> initialCounters,
        boolean grantHaste,
        boolean exileAtEndStep,
        boolean sacrificeAtEndStep,
        boolean tappedAndAttacking,
        boolean trackWithSource,
        boolean createForTargetController,
        CardColor colorOverride,
        Set<Keyword> additionalKeywords,
        List<CardSubtype> creatureSubtypeOverride,
        boolean tapped
) implements CardEffect {

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters, boolean grantHaste,
            boolean exileAtEndStep, boolean sacrificeAtEndStep, boolean tappedAndAttacking,
            boolean trackWithSource, boolean createForTargetController,
            CardColor colorOverride, Set<Keyword> additionalKeywords) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                grantHaste, exileAtEndStep, sacrificeAtEndStep, tappedAndAttacking,
                trackWithSource, createForTargetController, colorOverride, additionalKeywords, List.of(), false);
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters, boolean grantHaste,
            boolean exileAtEndStep, boolean sacrificeAtEndStep, boolean tappedAndAttacking,
            boolean trackWithSource, boolean createForTargetController,
            CardColor colorOverride, Set<Keyword> additionalKeywords,
            List<CardSubtype> creatureSubtypeOverride) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                grantHaste, exileAtEndStep, sacrificeAtEndStep, tappedAndAttacking,
                trackWithSource, createForTargetController, colorOverride, additionalKeywords,
                creatureSubtypeOverride, false);
    }

    public CreateTokenCopyOfTargetPermanentEffect() {
        this(List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                false, false, null, Set.of(), List.of(), false);
    }

    /** "except it has haste and 'At the beginning of the end step, exile this token.'" (Heat Shimmer). */
    public CreateTokenCopyOfTargetPermanentEffect(boolean grantHaste, boolean exileAtEndStep) {
        this(List.of(), Set.of(), null, null, Map.of(), grantHaste, exileAtEndStep, false, false,
                false, false, null, Set.of(), List.of(), false);
    }

    /**
     * "except it has haste and 'At the beginning of the end step, sacrifice this permanent.'" (Minion Reflector).
     * The sacrifice route (as opposed to {@code exileAtEndStep}) puts the token into the graveyard, so its own
     * dies-triggers and other players' "whenever a creature dies" abilities fire.
     */
    public CreateTokenCopyOfTargetPermanentEffect(boolean grantHaste, boolean exileAtEndStep, boolean sacrificeAtEndStep) {
        this(List.of(), Set.of(), null, null, Map.of(), grantHaste, exileAtEndStep, sacrificeAtEndStep, false,
                false, false, null, Set.of(), List.of(), false);
    }

    /** "Create a tapped and attacking token that's a copy of the target permanent." */
    public CreateTokenCopyOfTargetPermanentEffect(boolean grantHaste, boolean exileAtEndStep,
                                                  boolean sacrificeAtEndStep, boolean tappedAndAttacking) {
        this(List.of(), Set.of(), null, null, Map.of(), grantHaste, exileAtEndStep,
                sacrificeAtEndStep, tappedAndAttacking, false, false, null, Set.of(), List.of(), false);
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Integer powerOverride,
            Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                false, false, false, false, false, false, null, Set.of(), List.of(), false);
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Integer powerOverride,
            Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters,
            CardColor colorOverride,
            Set<Keyword> additionalKeywords) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                false, false, false, false, false, false, colorOverride, additionalKeywords, List.of(), false);
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes,
            Set<CardType> additionalTypes,
            Integer powerOverride,
            Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters,
            boolean grantHaste,
            boolean exileAtEndStep,
            boolean sacrificeAtEndStep,
            boolean tappedAndAttacking) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                grantHaste, exileAtEndStep, sacrificeAtEndStep, tappedAndAttacking,
                false, false, null, Set.of(), List.of(), false);
    }

    /** Creates a copy with the supplied creature subtypes replacing the copied creature types. */
    public static CreateTokenCopyOfTargetPermanentEffect withCreatureSubtypeOverride(
            List<CardSubtype> creatureSubtypes, CardColor colorOverride, int power, int toughness) {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), power, toughness, Map.of(), false, false, false, false,
                false, false, colorOverride, Set.of(), creatureSubtypes, false);
    }

    /** Used by global enter triggers that create a tracked copy for the entering creature's controller. */
    public static CreateTokenCopyOfTargetPermanentEffect trackedForTargetController() {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                true, true, null, Set.of(), List.of(), false);
    }

    public static CreateTokenCopyOfTargetPermanentEffect tappedTokenCopy() {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                false, false, null, Set.of(), List.of(), true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
