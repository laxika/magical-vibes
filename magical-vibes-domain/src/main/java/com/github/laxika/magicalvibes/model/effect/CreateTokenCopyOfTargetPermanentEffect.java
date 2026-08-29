package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

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
        boolean sacrificeAtNextUpkeep,
        Map<EffectSlot, List<CardEffect>> additionalSlotEffects,
        List<CardSubtype> creatureSubtypeOverride,
        boolean tapped,
        boolean removeLegendary,
        DynamicAmount amount
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
                trackWithSource, createForTargetController, colorOverride, additionalKeywords,
                false, Map.of(), List.of(), false, false, new Fixed(1));
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
                false, Map.of(), creatureSubtypeOverride, false, false, new Fixed(1));
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters, boolean grantHaste,
            boolean exileAtEndStep, boolean sacrificeAtEndStep, boolean tappedAndAttacking,
            boolean trackWithSource, boolean createForTargetController,
            CardColor colorOverride, Set<Keyword> additionalKeywords,
            boolean sacrificeAtNextUpkeep,
            Map<EffectSlot, List<CardEffect>> additionalSlotEffects) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                grantHaste, exileAtEndStep, sacrificeAtEndStep, tappedAndAttacking,
                trackWithSource, createForTargetController, colorOverride, additionalKeywords,
                sacrificeAtNextUpkeep, additionalSlotEffects, List.of(), false, false, new Fixed(1));
    }

    public CreateTokenCopyOfTargetPermanentEffect() {
        this(List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                false, false, null, Set.of(), false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    /** Creates {@code amount} copies of each targeted permanent. */
    public CreateTokenCopyOfTargetPermanentEffect(DynamicAmount amount) {
        this(List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                false, false, null, Set.of(), false, Map.of(), List.of(), false, false, amount);
    }

    public CreateTokenCopyOfTargetPermanentEffect(boolean grantHaste, boolean exileAtEndStep) {
        this(List.of(), Set.of(), null, null, Map.of(), grantHaste, exileAtEndStep, false, false,
                false, false, null, Set.of(), false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            boolean grantHaste, boolean exileAtEndStep, boolean sacrificeAtEndStep) {
        this(List.of(), Set.of(), null, null, Map.of(), grantHaste, exileAtEndStep,
                sacrificeAtEndStep, false, false, false, null, Set.of(),
                false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            boolean grantHaste, boolean exileAtEndStep,
            boolean sacrificeAtEndStep, boolean tappedAndAttacking) {
        this(List.of(), Set.of(), null, null, Map.of(), grantHaste, exileAtEndStep,
                sacrificeAtEndStep, tappedAndAttacking, false, false, null, Set.of(),
                false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                false, false, false, false, false, false, null, Set.of(),
                false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters, CardColor colorOverride,
            Set<Keyword> additionalKeywords) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                false, false, false, false, false, false, colorOverride, additionalKeywords,
                false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            int amount, List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters, CardColor colorOverride,
            Set<Keyword> additionalKeywords) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                false, false, false, false, false, false, colorOverride, additionalKeywords,
                false, Map.of(), List.of(), false, false, new Fixed(amount));
    }

    public CreateTokenCopyOfTargetPermanentEffect(
            List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters, boolean grantHaste,
            boolean exileAtEndStep, boolean sacrificeAtEndStep, boolean tappedAndAttacking) {
        this(additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                grantHaste, exileAtEndStep, sacrificeAtEndStep, tappedAndAttacking,
                false, false, null, Set.of(), false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    public static CreateTokenCopyOfTargetPermanentEffect withAdditionalEffects(
            boolean sacrificeAtNextUpkeep, Map<EffectSlot, List<CardEffect>> additionalSlotEffects) {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                false, false, null, Set.of(), sacrificeAtNextUpkeep, additionalSlotEffects,
                List.of(), false, false, new Fixed(1));
    }

    public static CreateTokenCopyOfTargetPermanentEffect withCreatureSubtypeOverride(
            List<CardSubtype> creatureSubtypes, CardColor colorOverride, int power, int toughness) {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), power, toughness, Map.of(), false, false, false, false,
                false, false, colorOverride, Set.of(), false, Map.of(),
                creatureSubtypes, false, false, new Fixed(1));
    }

    public static CreateTokenCopyOfTargetPermanentEffect trackedForTargetController() {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                true, true, null, Set.of(), false, Map.of(), List.of(), false, false, new Fixed(1));
    }

    public static CreateTokenCopyOfTargetPermanentEffect tappedTokenCopy() {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, false, false,
                false, false, null, Set.of(), false, Map.of(), List.of(), true, false, new Fixed(1));
    }

    /** Creates a copy with the supplied overrides that is not legendary. */
    public static CreateTokenCopyOfTargetPermanentEffect nonLegendary(
            List<CardSubtype> additionalSubtypes, Set<CardType> additionalTypes,
            Integer powerOverride, Integer toughnessOverride,
            Map<CounterType, Integer> initialCounters) {
        return new CreateTokenCopyOfTargetPermanentEffect(
                additionalSubtypes, additionalTypes, powerOverride, toughnessOverride, initialCounters,
                false, false, false, false, false, false, null, Set.of(), false, Map.of(),
                List.of(), false, true, new Fixed(1));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
