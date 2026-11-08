package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

import java.util.List;
import java.util.Set;

/**
 * Causes the source permanent to become a copy of the target creature until end of turn.
 * At the cleanup step, the permanent reverts to its original card.
 * Used by Tilonalli's Skinshifter and similar shapeshifters. {@link #ofTriggeringPermanent()} copies the stack
 * entry's triggering permanent instead, not targeted ("become a copy of that creature", Renegade Doppelganger): as
 * it is while it is on the battlefield, else as it last existed there.
 *
 * @param triggeringPermanent whether the creature copied is the stack entry's triggering permanent rather than its
 *                            {@code targetId}
 */
public record BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
        String nameOverride,
        Set<CardSupertype> additionalSupertypesOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        Set<CardType> additionalTypesOverride,
        Set<CardSubtype> additionalSubtypesOverride,
        Set<Keyword> additionalKeywordsOverride,
        Trigger retainedTrigger,
        List<Ability> additionalAbilitiesOverride,
        boolean triggeringPermanent
) implements TemporaryCopyEffect {

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect {
        additionalSupertypesOverride = additionalSupertypesOverride == null
                ? Set.of() : Set.copyOf(additionalSupertypesOverride);
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
        additionalSubtypesOverride = additionalSubtypesOverride == null
                ? Set.of() : Set.copyOf(additionalSubtypesOverride);
        additionalKeywordsOverride = additionalKeywordsOverride == null
                ? Set.of() : Set.copyOf(additionalKeywordsOverride);
        additionalAbilitiesOverride = additionalAbilitiesOverride == null
                ? List.of() : List.copyOf(additionalAbilitiesOverride);
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            String nameOverride, Set<CardSupertype> additionalSupertypesOverride,
            Integer powerOverride, Integer toughnessOverride,
            Set<CardType> additionalTypesOverride, Set<CardSubtype> additionalSubtypesOverride,
            Set<Keyword> additionalKeywordsOverride, Trigger retainedTrigger,
            List<Ability> additionalAbilitiesOverride) {
        this(nameOverride, additionalSupertypesOverride, powerOverride, toughnessOverride,
                additionalTypesOverride, additionalSubtypesOverride, additionalKeywordsOverride, retainedTrigger,
                additionalAbilitiesOverride, false);
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            String nameOverride, Set<CardSupertype> additionalSupertypesOverride,
            Integer powerOverride, Integer toughnessOverride,
            Set<CardType> additionalTypesOverride, Set<CardSubtype> additionalSubtypesOverride,
            Set<Keyword> additionalKeywordsOverride) {
        this(nameOverride, additionalSupertypesOverride, powerOverride, toughnessOverride,
                additionalTypesOverride, additionalSubtypesOverride, additionalKeywordsOverride, null, List.of());
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            String nameOverride, Set<CardSupertype> additionalSupertypesOverride) {
        this(nameOverride, additionalSupertypesOverride, null, null,
                Set.of(), Set.of(), Set.of(), null, List.of());
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            Integer powerOverride, Integer toughnessOverride,
            Set<CardType> additionalTypesOverride, Set<CardSubtype> additionalSubtypesOverride,
            Set<Keyword> additionalKeywordsOverride) {
        this(null, Set.of(), powerOverride, toughnessOverride, additionalTypesOverride,
                additionalSubtypesOverride, additionalKeywordsOverride, null, List.of());
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect() {
        this(null, Set.of(), null, null, Set.of(), Set.of(), Set.of(), null, List.of());
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            List<Ability> additionalAbilitiesOverride) {
        this(null, Set.of(), null, null, Set.of(), Set.of(), Set.of(), null, additionalAbilitiesOverride);
    }

    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
            String nameOverride, Set<CardSupertype> additionalSupertypesOverride,
            Integer powerOverride, Integer toughnessOverride,
            Set<CardType> additionalTypesOverride, Set<CardSubtype> additionalSubtypesOverride,
            Set<Keyword> additionalKeywordsOverride, Trigger retainedTrigger) {
        this(nameOverride, additionalSupertypesOverride, powerOverride, toughnessOverride,
                additionalTypesOverride, additionalSubtypesOverride, additionalKeywordsOverride,
                retainedTrigger, List.of());
    }

    /** This effect copying the stack entry's triggering permanent, not a target. */
    public BecomeCopyOfTargetCreatureUntilEndOfTurnEffect ofTriggeringPermanent() {
        return new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(nameOverride, additionalSupertypesOverride,
                powerOverride, toughnessOverride, additionalTypesOverride, additionalSubtypesOverride,
                additionalKeywordsOverride, retainedTrigger, additionalAbilitiesOverride, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return triggeringPermanent ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.permanent());
    }
}
