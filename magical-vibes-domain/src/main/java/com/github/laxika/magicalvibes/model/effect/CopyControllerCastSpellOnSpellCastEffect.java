package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.Set;

/**
 * Trigger descriptor for {@code ON_CONTROLLER_CASTS_SPELL}: whenever the controller casts a spell
 * matching {@code spellFilter}, they may pay a cost to copy that spell. The copy's controller may
 * choose new targets.
 * <p>
 * At trigger time, {@code SpellCastTriggerCollectorService} snapshots the cast spell and places the
 * copy effect on the stack, wrapped in a "may pay" effect: {@link MayPayTapPermanentsEffect} when
 * {@code tapCost} is non-null (Aziza, Mage Tower Captain), or {@link MayPayManaEffect} when
 * {@code manaCost} is non-null (Cloven Casting). A non-null {@code sacrificeFilter} makes the
 * copy conditional on sacrificing a matching permanent during resolution. At most one of these
 * optional costs should be set. With no cost the copy is mandatory, unless the effect is wrapped
 * in a {@link MayEffect} (Swarm Intelligence).
 *
 * @param requiredCastZone the zone the spell must have been cast from for the trigger to fire, or
 *                         {@code null} for any zone. {@link Zone#LIBRARY} covers "whenever you cast
 *                         an instant or sorcery spell from your library" (Melek, Izzet Paragon)
 * @param sacrificeFilter restricts the permanent that may be sacrificed before creating the copy,
 *                        or {@code null} for no sacrifice requirement
 * @param sacrificeDescription description used in the sacrifice choice prompt
 */
public record CopyControllerCastSpellOnSpellCastEffect(
        CardPredicate spellFilter,
        TapMultiplePermanentsCost tapCost,
        String manaCost,
        Zone requiredCastZone,
        StackEntryPredicate castSpellTargetCondition,
        Set<Keyword> grantedKeywords,
        PermanentPredicate sacrificeFilter,
        String sacrificeDescription
) implements CardEffect {

    public CopyControllerCastSpellOnSpellCastEffect {
        grantedKeywords = grantedKeywords == null ? Set.of() : Set.copyOf(grantedKeywords);
    }

    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, TapMultiplePermanentsCost tapCost,
            String manaCost) {
        this(spellFilter, tapCost, manaCost, null, null, Set.of(), null, null);
    }

    /** "you may tap N creatures. If you do, copy that spell" (Aziza, Mage Tower Captain). */
    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, TapMultiplePermanentsCost tapCost) {
        this(spellFilter, tapCost, null, null, null, Set.of(), null, null);
    }

    /** "you may pay {cost}. If you do, copy that spell" (Cloven Casting). */
    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, String manaCost) {
        this(spellFilter, null, manaCost, null, null, Set.of(), null, null);
    }

    /** "whenever you cast a [filter] spell from your library, copy it" (Melek, Izzet Paragon). */
    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, Zone requiredCastZone) {
        this(spellFilter, null, null, requiredCastZone, null, Set.of(), null, null);
    }

    /** "you may sacrifice a [permanent]. If you do, copy that spell" (Mica, Reader of Ruins). */
    public static CopyControllerCastSpellOnSpellCastEffect withSacrificeFilter(
            CardPredicate spellFilter,
            PermanentPredicate sacrificeFilter, String sacrificeDescription) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, null, Set.of(), sacrificeFilter, sacrificeDescription);
    }

    public static CopyControllerCastSpellOnSpellCastEffect withCastTargetCondition(
            CardPredicate spellFilter, StackEntryPredicate castSpellTargetCondition,
            Set<Keyword> grantedKeywords) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, castSpellTargetCondition, grantedKeywords, null, null);
    }
}
