package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * "You may have this creature enter as a copy of any {@code typeLabel}" (Clone, Sculpting Steel).
 *
 * <p>The last three embalm fields carry the Vizier-of-Many-Faces embalm exception ("except if this
 * creature was embalmed, the token has no mana cost, it's white, and it's a Zombie in addition to
 * its other types"). They are applied to the final copy — after the chosen permanent's copiable
 * values overwrite the entering object — but only when the entering permanent is an embalm token,
 * so a hard-cast Clone keeps the copied creature's own color/cost/types. All three are inert
 * ({@code null}/{@code false}) for a plain copy.
 *
 * <p>{@code additionalPlusOnePlusOneCounters} covers "except it enters with X additional +1/+1
 * counters" (Altered Ego). Counters are applied only when the controller chooses to copy; declining
 * leaves a 0/0 with no counters from this effect.
 *
 * <p>{@code additionalSubtypesOverride} and {@code additionalSlotEffects} cover "except it's an
 * [subtype] in addition to its other types and it has '[triggered ability]'" (Phantasmal Image).
 * Both are applied to the final copy only when the controller chooses to copy.
 *
 * <p>{@code copyPowerToughnessFromSource} covers copy exceptions whose power and toughness come
 * from the entering card's cast-mode characteristics rather than the chosen permanent.
 *
 * <p>{@code nameOverride} and {@code additionalSupertypesOverride} cover copy exceptions that
 * retain the entering card's name or add a supertype to the final copy (Sakashima the Impostor).
 */
public record CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                         Integer toughnessOverride,
                                         Set<CardType> additionalTypesOverride,
                                         List<ActivatedAbility> additionalActivatedAbilities,
                                         CardColor embalmColorOverride, CardSubtype embalmAddedSubtype,
                                         boolean embalmRemoveManaCost,
                                         DynamicAmount additionalPlusOnePlusOneCounters,
                                         Set<CardSubtype> additionalSubtypesOverride,
                                         Map<EffectSlot, List<CardEffect>> additionalSlotEffects,
                                         boolean copyPowerToughnessFromSource,
                                         String nameOverride,
                                         Set<CardSupertype> additionalSupertypesOverride,
                                         Set<Keyword> additionalKeywordsOverride,
                                         boolean additionalCreatureOnlyCharacteristics,
                                         boolean copyColor,
                                         Set<CardSupertype> removedSupertypesOverride,
                                         boolean addTypeAppropriateCounters) implements ReplacementEffect {

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false, null, Set.of(), Map.of(), false,
                null, Set.of(), Set.of(), false, true, Set.of(), false);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      DynamicAmount additionalPlusOnePlusOneCounters) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false,
                additionalPlusOnePlusOneCounters, Set.of(), Map.of(), false, null, Set.of(), Set.of(), false, true,
                Set.of(), false);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride) {
        this(filter, typeLabel, powerOverride, toughnessOverride, Set.of(), List.of(), null, null, false, null,
                Set.of(), Map.of(), false, null, Set.of(), Set.of(), false, true, Set.of(), false);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        this(filter, typeLabel, powerOverride, toughnessOverride, additionalTypesOverride, List.of(), null, null,
                false, null, Set.of(), Map.of(), false, null, Set.of(), Set.of(), false, true, Set.of(), false);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      Set<CardType> additionalTypesOverride,
                                      boolean copyPowerToughnessFromSource) {
        this(filter, typeLabel, null, null, additionalTypesOverride, List.of(), null, null,
                false, null, Set.of(), Map.of(), copyPowerToughnessFromSource, null, Set.of(), Set.of(), false, true,
                Set.of(), false);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride, Set<CardType> additionalTypesOverride,
                                      List<ActivatedAbility> additionalActivatedAbilities) {
        this(filter, typeLabel, powerOverride, toughnessOverride, additionalTypesOverride,
                additionalActivatedAbilities, null, null, false, null, Set.of(), Map.of(), false, null, Set.of(), Set.of(), false, true,
                Set.of(), false);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      String nameOverride, Set<CardSupertype> additionalSupertypesOverride,
                                      List<ActivatedAbility> additionalActivatedAbilities) {
        this(filter, typeLabel, null, null, Set.of(), additionalActivatedAbilities, null, null, false, null,
                Set.of(), Map.of(), false, nameOverride, additionalSupertypesOverride, Set.of(), false, true,
                Set.of(), false);
    }

    /** Clone with the embalm exception (Vizier of Many Faces): copy a creature, but an embalm token
     *  becomes the given color, gains the given creature type, and loses its mana cost. */
    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      CardColor embalmColorOverride, CardSubtype embalmAddedSubtype,
                                      boolean embalmRemoveManaCost) {
        this(filter, typeLabel, null, null, Set.of(), List.of(),
                embalmColorOverride, embalmAddedSubtype, embalmRemoveManaCost, null, Set.of(), Map.of(), false,
                null, Set.of(), Set.of(), false, true, Set.of(), false);
    }

    /** Clone that also gains creature types and triggered/static abilities of its own (Phantasmal Image). */
    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      Set<CardSubtype> additionalSubtypesOverride,
                                      Map<EffectSlot, List<CardEffect>> additionalSlotEffects) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false, null,
                additionalSubtypesOverride, additionalSlotEffects, false, null, Set.of(), Set.of(), false, true,
                Set.of(), false);
    }

    /** Clone that also adds copy exceptions to the resulting permanent and may omit its color. */
    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      Set<CardSubtype> additionalSubtypesOverride,
                                      Map<EffectSlot, List<CardEffect>> additionalSlotEffects,
                                      boolean copyColor) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false, null,
                additionalSubtypesOverride, additionalSlotEffects, false, null, Set.of(), Set.of(), false, copyColor,
                Set.of(), false);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      Set<CardSupertype> additionalSupertypesOverride,
                                      Set<Keyword> additionalKeywordsOverride,
                                      DynamicAmount additionalPlusOnePlusOneCounters,
                                      boolean additionalCreatureOnlyCharacteristics) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false,
                additionalPlusOnePlusOneCounters, Set.of(), Map.of(), false, null,
                additionalSupertypesOverride, additionalKeywordsOverride,
                additionalCreatureOnlyCharacteristics, true, Set.of(), false);
    }

    /** Clone that removes a supertype and adds the appropriate creature or planeswalker counter. */
    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      CardSupertype removedSupertype,
                                      boolean addTypeAppropriateCounters) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false, null,
                Set.of(), Map.of(), false, null, Set.of(), Set.of(), false, true,
                Set.of(removedSupertype), addTypeAppropriateCounters);
    }
}
