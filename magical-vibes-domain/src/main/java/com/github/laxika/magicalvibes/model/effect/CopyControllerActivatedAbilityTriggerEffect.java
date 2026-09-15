package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Trigger descriptor for ability-activation slots: copy an activated ability when its trigger
 * condition matches. The copy's controller may choose new targets for the copy. The normal form is
 * used in {@code ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY}; the {@code activationCostContainsX}
 * form is also used in {@code ON_CONTROLLER_ACTIVATES_ABILITY} for triggers that include mana
 * abilities.
 * <p>
 * At trigger time, the relevant trigger collector snapshots the activated ability on the stack and places a
 * {@link CopyControllerActivatedAbilityEffect} on the stack, wrapped in a {@link MayPayManaEffect}
 * when {@code manaCost} is non-null.
 * <p>
 * Used by Rings of Brighthearth ({@code sourceFilter == null}), Kurkesh, Onakke Ancient
 * (a {@code StackEntryCardTypeInPredicate(ARTIFACT)} source filter, so only abilities of
 * artifacts trigger it) and Illusionist's Bracers ({@code manaCost == null} plus
 * {@code equippedCreatureOnly} — the equipped creature's abilities are copied for free).
 *
 * Chandra's Regulator uses {@code loyaltyAbilityOnly} to limit the trigger to loyalty abilities.
 *
 * @param manaCost             the cost the controller may pay to create the copy (e.g. {@code "{2}"});
 *                             {@code null} makes the copy free and mandatory
 * @param sourceFilter         optional restriction on the activated ability's stack entry (its card is
 *                             the ability's source); {@code null} means every non-mana ability triggers
 * @param equippedCreatureOnly when {@code true} the trigger fires only for abilities of the creature
 *                             this permanent is attached to (whoever activated them) instead of for
 *                             abilities activated by this permanent's controller
 * @param loyaltyAbilityOnly  when {@code true} the trigger fires only for loyalty abilities
 * @param targetPredicate      optional restriction on the activated ability's chosen targets
 * @param activationCostContainsX when {@code true}, the trigger fires only when the activated
 *                                ability's mana activation cost contains {@code X}
 */
public record CopyControllerActivatedAbilityTriggerEffect(
        String manaCost,
        StackEntryPredicate sourceFilter,
        boolean equippedCreatureOnly,
        boolean loyaltyAbilityOnly,
        StackEntryPredicate targetPredicate,
        boolean activationCostContainsX
) implements CardEffect {

    public CopyControllerActivatedAbilityTriggerEffect(String manaCost) {
        this(manaCost, null, false, false, null, false);
    }

    public CopyControllerActivatedAbilityTriggerEffect(String manaCost, StackEntryPredicate sourceFilter) {
        this(manaCost, sourceFilter, false, false, null, false);
    }

    public CopyControllerActivatedAbilityTriggerEffect(String manaCost, StackEntryPredicate sourceFilter,
                                                       boolean equippedCreatureOnly) {
        this(manaCost, sourceFilter, equippedCreatureOnly, false, null, false);
    }

    public CopyControllerActivatedAbilityTriggerEffect(String manaCost, StackEntryPredicate sourceFilter,
                                                       boolean equippedCreatureOnly, boolean loyaltyAbilityOnly) {
        this(manaCost, sourceFilter, equippedCreatureOnly, loyaltyAbilityOnly, null, false);
    }

    public CopyControllerActivatedAbilityTriggerEffect(String manaCost, StackEntryPredicate sourceFilter,
                                                       boolean equippedCreatureOnly, boolean loyaltyAbilityOnly,
                                                       StackEntryPredicate targetPredicate) {
        this(manaCost, sourceFilter, equippedCreatureOnly, loyaltyAbilityOnly, targetPredicate, false);
    }
}
