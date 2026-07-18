package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "… a source of your choice would deal damage …" prevention: on resolution the controller chooses
 * a damage source from the battlefield (optionally restricted by {@code sourceFilter}) and a
 * prevention shield keyed to that source is installed. {@code scope} selects the shield:
 *
 * <ul>
 *   <li>{@link ChosenSourcePreventionScope#NEXT_DAMAGE_TO_CONTROLLER} — one-shot, protects the
 *       controller; {@code gainLife} additionally gains life equal to the prevented damage
 *       (Reverse Damage; plain: Pentagram of the Ages, the Circle of Protection cycle).</li>
 *   <li>{@link ChosenSourcePreventionScope#NEXT_DAMAGE_TO_ANY_TARGET} — one-shot, protects
 *       whatever the source would damage next (Sanctum Guardian).</li>
 *   <li>{@link ChosenSourcePreventionScope#ALL_DAMAGE_THIS_TURN} — lasts the turn;
 *       {@code controllerOnly} shields just the controller (Auriok Replica) versus everything
 *       (Burrenton Forge-Tender).</li>
 * </ul>
 *
 * @param scope          which shield is installed for the chosen source
 * @param gainLife       NEXT_DAMAGE_TO_CONTROLLER only: gain life equal to the prevented damage
 * @param controllerOnly ALL_DAMAGE_THIS_TURN only: shield only the controller, not all recipients
 * @param sourceFilter   restricts which permanents are legal source choices; {@code null} = any
 * @param sourceLabel    human-readable label for the restriction, used in the choice prompt
 *                       ("red", "artifact"); {@code null} = unrestricted wording
 */
public record PreventDamageFromChosenSourceEffect(
        ChosenSourcePreventionScope scope,
        boolean gainLife,
        boolean controllerOnly,
        PermanentPredicate sourceFilter,
        String sourceLabel) implements CardEffect {

    /** "The next time a source of your choice would deal damage to you this turn, prevent that damage." */
    public static PreventDamageFromChosenSourceEffect nextDamageToYou() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, false, false, null, null);
    }

    /** Reverse Damage: as {@link #nextDamageToYou()}, plus "you gain life equal to the damage prevented". */
    public static PreventDamageFromChosenSourceEffect nextDamageToYouAndGainLife() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, true, false, null, null);
    }

    /** Circle of Protection cycle: only sources matching {@code sourceFilter} may be chosen. */
    public static PreventDamageFromChosenSourceEffect nextDamageToYou(PermanentPredicate sourceFilter,
                                                                      String sourceLabel) {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, false, false, sourceFilter, sourceLabel);
    }

    /** Sanctum Guardian: the chosen source's next damage to any target is prevented. */
    public static PreventDamageFromChosenSourceEffect nextDamageToAnyTarget() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_ANY_TARGET, false, false, null, null);
    }

    /** Auriok Replica: prevent all damage the chosen source would deal to you this turn. */
    public static PreventDamageFromChosenSourceEffect allDamageToYou() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, true, null, null);
    }

    /** Burrenton Forge-Tender: prevent all damage a matching chosen source would deal this turn. */
    public static PreventDamageFromChosenSourceEffect allDamage(PermanentPredicate sourceFilter,
                                                                String sourceLabel) {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, false, sourceFilter, sourceLabel);
    }
}
