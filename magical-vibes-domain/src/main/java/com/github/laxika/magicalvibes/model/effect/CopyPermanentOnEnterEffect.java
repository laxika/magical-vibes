package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
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
 */
public record CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                         Integer toughnessOverride,
                                         Set<CardType> additionalTypesOverride,
                                         List<ActivatedAbility> additionalActivatedAbilities,
                                         CardColor embalmColorOverride, CardSubtype embalmAddedSubtype,
                                         boolean embalmRemoveManaCost,
                                         DynamicAmount additionalPlusOnePlusOneCounters) implements ReplacementEffect {

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false, null);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      DynamicAmount additionalPlusOnePlusOneCounters) {
        this(filter, typeLabel, null, null, Set.of(), List.of(), null, null, false, additionalPlusOnePlusOneCounters);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride) {
        this(filter, typeLabel, powerOverride, toughnessOverride, Set.of(), List.of(), null, null, false, null);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride, Set<CardType> additionalTypesOverride) {
        this(filter, typeLabel, powerOverride, toughnessOverride, additionalTypesOverride, List.of(), null, null, false, null);
    }

    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel, Integer powerOverride,
                                      Integer toughnessOverride, Set<CardType> additionalTypesOverride,
                                      List<ActivatedAbility> additionalActivatedAbilities) {
        this(filter, typeLabel, powerOverride, toughnessOverride, additionalTypesOverride,
                additionalActivatedAbilities, null, null, false, null);
    }

    /** Clone with the embalm exception (Vizier of Many Faces): copy a creature, but an embalm token
     *  becomes the given color, gains the given creature type, and loses its mana cost. */
    public CopyPermanentOnEnterEffect(PermanentPredicate filter, String typeLabel,
                                      CardColor embalmColorOverride, CardSubtype embalmAddedSubtype,
                                      boolean embalmRemoveManaCost) {
        this(filter, typeLabel, null, null, Set.of(), List.of(),
                embalmColorOverride, embalmAddedSubtype, embalmRemoveManaCost, null);
    }
}
