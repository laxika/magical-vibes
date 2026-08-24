package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
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
 * {@code manaCost} is non-null (Cloven Casting). At most one cost should be set. With neither cost
 * the copy is mandatory, unless the effect is wrapped in a {@link MayEffect} (Swarm Intelligence).
 *
 * @param requiredCastZone the zone the spell must have been cast from for the trigger to fire, or
 *                         {@code null} for any zone. {@link Zone#LIBRARY} covers "whenever you cast
 *                         an instant or sorcery spell from your library" (Melek, Izzet Paragon)
 * @param intervening       optional source condition checked when the spell is cast and again when
 *                          the copy trigger resolves
 * @param additionalTypes  types added to the copied card, such as artifact for Tawnos, the Toymaker
 * @param tokenCopy        whether the copied creature spell becomes a token as it resolves
 */
public record CopyControllerCastSpellOnSpellCastEffect(
        CardPredicate spellFilter,
        TapMultiplePermanentsCost tapCost,
        String manaCost,
        Zone requiredCastZone,
        StackEntryPredicate castSpellTargetCondition,
        Set<Keyword> grantedKeywords,
        Condition intervening,
        Set<CardType> additionalTypes,
        boolean tokenCopy,
        boolean mayChooseNewTargets,
        boolean grantHasteToPermanentSpell,
        boolean excludeHandCast
) implements CardEffect {

    public CopyControllerCastSpellOnSpellCastEffect {
        grantedKeywords = grantedKeywords == null ? Set.of() : Set.copyOf(grantedKeywords);
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
    }

    public CopyControllerCastSpellOnSpellCastEffect(
            CardPredicate spellFilter, TapMultiplePermanentsCost tapCost, String manaCost,
            Zone requiredCastZone, StackEntryPredicate castSpellTargetCondition,
            Set<Keyword> grantedKeywords, Condition intervening, Set<CardType> additionalTypes,
            boolean tokenCopy, boolean mayChooseNewTargets) {
        this(spellFilter, tapCost, manaCost, requiredCastZone, castSpellTargetCondition,
                grantedKeywords, intervening, additionalTypes, tokenCopy, mayChooseNewTargets,
                false, false);
    }

    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, TapMultiplePermanentsCost tapCost,
            String manaCost) {
        this(spellFilter, tapCost, manaCost, null, null, Set.of(), null, Set.of(), false, true, false, false);
    }

    /** "you may tap N creatures. If you do, copy that spell" (Aziza, Mage Tower Captain). */
    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, TapMultiplePermanentsCost tapCost) {
        this(spellFilter, tapCost, null, null, null, Set.of(), null, Set.of(), false, true, false, false);
    }

    /** "you may pay {cost}. If you do, copy that spell" (Cloven Casting). */
    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, String manaCost) {
        this(spellFilter, null, manaCost, null, null, Set.of(), null, Set.of(), false, true, false, false);
    }

    /** "whenever you cast a [filter] spell from your library, copy it" (Melek, Izzet Paragon). */
    public CopyControllerCastSpellOnSpellCastEffect(CardPredicate spellFilter, Zone requiredCastZone) {
        this(spellFilter, null, null, requiredCastZone, null, Set.of(), null, Set.of(), false, true, false, false);
    }

    public static CopyControllerCastSpellOnSpellCastEffect withCastTargetCondition(
            CardPredicate spellFilter, StackEntryPredicate castSpellTargetCondition,
            Set<Keyword> grantedKeywords) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, castSpellTargetCondition, grantedKeywords,
                null, Set.of(), false, true, false, false);
    }

    /** Free optional copy trigger with a source-relative intervening condition. */
    public static CopyControllerCastSpellOnSpellCastEffect withIntervening(
            CardPredicate spellFilter, Condition intervening) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, null, Set.of(), intervening, Set.of(), false, true, false, false);
    }

    /** "Whenever you cast a matching creature spell, you may copy it as an artifact token." */
    public static CopyControllerCastSpellOnSpellCastEffect asArtifactToken(CardPredicate spellFilter) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, null, Set.of(), null,
                Set.of(CardType.ARTIFACT), true, true, false, false);
    }

    public CopyControllerCastSpellOnSpellCastEffect(
            CardPredicate spellFilter, TapMultiplePermanentsCost tapCost, String manaCost,
            Zone requiredCastZone, StackEntryPredicate castSpellTargetCondition,
            Set<Keyword> grantedKeywords, Set<CardType> additionalTypes, boolean tokenCopy) {
        this(spellFilter, tapCost, manaCost, requiredCastZone, castSpellTargetCondition,
                grantedKeywords, null, additionalTypes, tokenCopy, true, false, false);
    }

    /** Optional mana-paid copy of a permanent spell that enters as a token without retargeting. */
    public static CopyControllerCastSpellOnSpellCastEffect tokenCopy(
            CardPredicate spellFilter, String manaCost, Condition intervening) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, manaCost, null, null, Set.of(), intervening, Set.of(), true, false, false, false);
    }

    /** Free optional copy trigger for spells cast from any zone other than the hand. */
    public static CopyControllerCastSpellOnSpellCastEffect fromOutsideHand(CardPredicate spellFilter) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, null, Set.of(), null, Set.of(), false, true, false, true);
    }

    /** Free optional copy trigger that gives permanent spell copies haste. */
    public static CopyControllerCastSpellOnSpellCastEffect withPermanentSpellHaste(CardPredicate spellFilter) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, null, Set.of(), null, Set.of(), false, true, true, false);
    }

    /** Free optional copy trigger for non-hand casts whose permanent copies gain haste. */
    public static CopyControllerCastSpellOnSpellCastEffect fromOutsideHandWithPermanentSpellHaste(
            CardPredicate spellFilter) {
        return new CopyControllerCastSpellOnSpellCastEffect(
                spellFilter, null, null, null, null, Set.of(), null, Set.of(), false, true, true, true);
    }
}
