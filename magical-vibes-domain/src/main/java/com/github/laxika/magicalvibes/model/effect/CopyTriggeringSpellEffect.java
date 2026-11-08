package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.SpellsCastBeforeTriggeringSpellThisTurn;

import java.util.List;
import java.util.Set;

/**
 * Creates a copy of the spell that caused the resolving spell-cast trigger: the spell still on the stack, or, once it
 * has left, the spell as it was when the ability triggered (the stack entry's triggering spell, CR 608.2h). The copy is
 * controlled by the spell's controller. Unless {@code mayChooseNewTargets} is cleared (a plain "copy it" with no "you
 * may choose new targets"), the copy's controller is asked whether to choose new targets for it.
 * <p>
 * The modifications are the "except …" of the copy and what the ability does to both spells: build them with the
 * {@code with…} methods ({@code new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens().withCopies(2)}).
 *
 * @param tokenCopy                  whether the copy becomes a token as it resolves, whatever kind of spell it is
 * @param retargetToSource           whether the ability's controller controls the copy and it targets the ability's
 *                                   source instead ("the copy targets Ivy")
 * @param mayChooseNewTargets        whether the copy's controller may choose new targets for it
 * @param permanentSpellToken        whether a copy of a permanent spell becomes a token as it resolves
 * @param grantHasteToPermanentSpell whether a copy of a permanent spell gains haste
 * @param sacrificeAtEndStep         whether a token copy has "At the beginning of the end step, sacrifice this
 *                                   permanent"
 * @param grantedKeywords            keywords the copied spell and the copy gain ("those spells gain wither")
 * @param additionalTypes            card types the copy has in addition to its other types
 * @param removedSupertypes          supertypes the copy doesn't have ("except the copy isn't legendary")
 * @param additionalSubtypes         subtypes the copy has in addition to its other types
 * @param powerOverride              the copy's base power, or {@code null} to keep the spell's
 * @param toughnessOverride          the copy's base toughness, or {@code null} to keep the spell's
 * @param copies                     how many copies the ability creates as one copy event ("copy it twice")
 * @param dynamicCopies              when set, how many copies, counted as the ability resolves ("copy it for each other
 *                                   instant and sorcery spell you've cast before it this turn"); no copy is made when
 *                                   it is zero or less, and {@code copies} is then unused
 * @param startingLoyaltyFromX       whether a copy of a planeswalker spell has the spell's chosen X as its starting
 *                                   loyalty ("except it has loyalty X", Ob Nixilis, the Adversary)
 */
public record CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource, boolean mayChooseNewTargets,
                                        boolean permanentSpellToken, boolean grantHasteToPermanentSpell,
                                        boolean sacrificeAtEndStep, Set<Keyword> grantedKeywords,
                                        Set<CardType> additionalTypes, Set<CardSupertype> removedSupertypes,
                                        List<CardSubtype> additionalSubtypes, Integer powerOverride,
                                        Integer toughnessOverride, int copies, DynamicAmount dynamicCopies,
                                        boolean startingLoyaltyFromX)
        implements TriggeringSpellReferencingEffect {

    public CopyTriggeringSpellEffect {
        grantedKeywords = grantedKeywords == null ? Set.of() : Set.copyOf(grantedKeywords);
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
        removedSupertypes = removedSupertypes == null ? Set.of() : Set.copyOf(removedSupertypes);
        additionalSubtypes = additionalSubtypes == null ? List.of() : List.copyOf(additionalSubtypes);
        if (copies < 1) {
            throw new IllegalArgumentException("A copy effect creates at least one copy, got " + copies);
        }
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource, boolean mayChooseNewTargets,
                                     boolean permanentSpellToken, boolean grantHasteToPermanentSpell,
                                     boolean sacrificeAtEndStep, Set<Keyword> grantedKeywords,
                                     Set<CardType> additionalTypes, Set<CardSupertype> removedSupertypes,
                                     List<CardSubtype> additionalSubtypes, Integer powerOverride,
                                     Integer toughnessOverride, int copies, DynamicAmount dynamicCopies) {
        this(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken, grantHasteToPermanentSpell,
                sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes, additionalSubtypes,
                powerOverride, toughnessOverride, copies, dynamicCopies, false);
    }

    public CopyTriggeringSpellEffect() {
        this(false, false, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy) {
        this(tokenCopy, false, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource) {
        this(tokenCopy, retargetToSource, true);
    }

    public CopyTriggeringSpellEffect(boolean tokenCopy, boolean retargetToSource, boolean mayChooseNewTargets) {
        this(tokenCopy, retargetToSource, mayChooseNewTargets, false, false, false, Set.of(), Set.of(), Set.of(),
                List.of(), null, null, 1, null, false);
    }

    /**
     * Storm's copy effect (CR 702.40a: "copy it for each other spell that was cast before it this turn"), the effect of
     * the spell's {@code Triggers.youCastThisSpell()} ability: as many copies as spells any player cast before the
     * triggering spell this turn, counted as the ability resolves up to the spell's place in the turn's cast order.
     */
    public static CopyTriggeringSpellEffect storm() {
        return new CopyTriggeringSpellEffect().withCopies(SpellsCastBeforeTriggeringSpellThisTurn.byAnyPlayer());
    }

    /** Creates a copy that is controlled by the trigger's controller and targets its source. */
    public static CopyTriggeringSpellEffect targetingSource() {
        return new CopyTriggeringSpellEffect(false, true, true);
    }

    /** The same copy effect, with the copy keeping the copied spell's targets (no new-target choice). */
    public CopyTriggeringSpellEffect withoutNewTargetChoice() {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, false, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /** The same copy effect, a copy of a permanent spell becoming a token. */
    public CopyTriggeringSpellEffect permanentSpellCopiesAreTokens() {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, true,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /** The same copy effect, a copy of a permanent spell gaining haste. */
    public CopyTriggeringSpellEffect permanentSpellCopiesGainHaste() {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                true, sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes, additionalSubtypes,
                powerOverride, toughnessOverride, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /** The same copy effect, a token copy having "At the beginning of the end step, sacrifice this permanent". */
    public CopyTriggeringSpellEffect sacrificedAtEndStep() {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, true, grantedKeywords, additionalTypes, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /** The same copy effect, the copied spell and the copy gaining {@code keywords} ("those spells gain wither"). */
    public CopyTriggeringSpellEffect withGrantedKeywords(Set<Keyword> keywords) {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, keywords, additionalTypes, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /** The same copy effect, the copy having {@code types} in addition to its other types. */
    public CopyTriggeringSpellEffect withAdditionalTypes(Set<CardType> types) {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, types, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /** The same copy effect, the copy not having {@code supertypes} ("except the copy isn't legendary"). */
    public CopyTriggeringSpellEffect withoutSupertypes(Set<CardSupertype> supertypes) {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, additionalTypes, supertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /**
     * The same copy effect, the copy being a {@code power}/{@code toughness} {@code subtypes} in addition to its other
     * types ("except the copy is a 1/1 Spirit in addition to its other types").
     */
    public CopyTriggeringSpellEffect asAdditionally(List<CardSubtype> subtypes, int power, int toughness) {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes,
                subtypes, power, toughness, copies, dynamicCopies, startingLoyaltyFromX);
    }

    /** The same copy effect, creating {@code copies} copies as one copy event ("copy it twice"). */
    public CopyTriggeringSpellEffect withCopies(int copies) {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, null, startingLoyaltyFromX);
    }

    /**
     * The same copy effect, creating as many copies as {@code copies} counts as the ability resolves, as one copy event
     * ("copy it for each other instant and sorcery spell you've cast before it this turn"); none when it counts zero.
     */
    public CopyTriggeringSpellEffect withCopies(DynamicAmount copies) {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, 1, copies, startingLoyaltyFromX);
    }

    /**
     * The same copy effect, a copy of a planeswalker spell having the spell's chosen X as its starting loyalty
     * ("except it has loyalty X").
     */
    public CopyTriggeringSpellEffect withStartingLoyaltyFromX() {
        return new CopyTriggeringSpellEffect(tokenCopy, retargetToSource, mayChooseNewTargets, permanentSpellToken,
                grantHasteToPermanentSpell, sacrificeAtEndStep, grantedKeywords, additionalTypes, removedSupertypes,
                additionalSubtypes, powerOverride, toughnessOverride, copies, dynamicCopies, true);
    }
}
