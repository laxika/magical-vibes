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
 *       whatever the source would damage next (Sanctum Guardian); {@code damageRedSourceController}
 *       adds Honorable Passage's red-source rider.</li>
 *   <li>{@link ChosenSourcePreventionScope#NEXT_DAMAGE_TO_CONTROLLER_AND_CREATURES} — one-shot,
 *       protects the controller and the creatures they control; life gain applies only when the
 *       chosen source is black (Shadowbane).</li>
 *   <li>{@link ChosenSourcePreventionScope#ALL_DAMAGE_THIS_TURN} — lasts the turn;
 *       {@code controllerOnly} shields just the controller (Auriok Replica) versus everything
 *       (Burrenton Forge-Tender).</li>
 * </ul>
 *
 * @param scope                       which shield is installed for the chosen source
 * @param gainLife                    NEXT_DAMAGE_TO_CONTROLLER only: gain life equal to the prevented damage
 * @param gainLifeForBlackOrRedSource ALL_DAMAGE_THIS_TURN/controllerOnly only: gain life equal to each
 *                                    prevented damage event from a black or red source
 * @param controllerOnly              ALL_DAMAGE_THIS_TURN only: shield only the controller, not all recipients
 * @param sourceFilter                restricts which permanents are legal source choices; {@code null} = any
 * @param sourceLabel                 human-readable label for the restriction, used in the choice prompt
 *                                    ("red", "artifact"); {@code null} = unrestricted wording
 * @param sourceChosenColor           restrict the choice to sources of the colour chosen for the ability's
 *                                    source permanent (Prismatic Circle; pair with {@link ChooseColorOnEnterEffect}).
 *                                    Resolved at resolution time, so it overrides {@code sourceFilter}/{@code sourceLabel}.
 * @param sourceSharesColorWithImprintedCard
 *                                    restrict the choice to sources sharing a colour with the card imprinted
 *                                    on the ability's source permanent
 * @param sourceActivationManaColor   restrict the choice to sources sharing a colour with mana spent on
 *                                    this activation (Protective Sphere)
 * @param exileFromLibrary            NEXT_DAMAGE_TO_CONTROLLER only: exile cards from the top of the
 *                                    controller's library equal to the damage prevented this way (Bone Mask)
 * @param damageRedSourceController   NEXT_DAMAGE_TO_ANY_TARGET only: if prevented damage is from a red
 *                                    source, this spell deals that much to the source's controller
 *                                    (Honorable Passage)
 * @param damageSourceController       NEXT_DAMAGE_TO_CONTROLLER only: this spell deals prevented damage
 *                                    to the chosen source's controller (Deflecting Palm)
 */
public record PreventDamageFromChosenSourceEffect(
        ChosenSourcePreventionScope scope,
        boolean gainLife,
        boolean gainLifeForBlackOrRedSource,
        boolean controllerOnly,
        PermanentPredicate sourceFilter,
        String sourceLabel,
        boolean sourceChosenColor,
        boolean sourceSharesColorWithImprintedCard,
        boolean sourceActivationManaColor,
        boolean exileFromLibrary,
        boolean damageRedSourceController,
        boolean damageSourceController) implements CardEffect {

    public PreventDamageFromChosenSourceEffect(
            ChosenSourcePreventionScope scope,
            boolean gainLife,
            boolean controllerOnly,
            PermanentPredicate sourceFilter,
            String sourceLabel,
            boolean sourceChosenColor,
            boolean exileFromLibrary,
            boolean damageRedSourceController) {
        this(scope, gainLife, false, controllerOnly, sourceFilter, sourceLabel, sourceChosenColor, false,
                false, exileFromLibrary, damageRedSourceController, false);
    }

    public PreventDamageFromChosenSourceEffect(
            ChosenSourcePreventionScope scope,
            boolean gainLife,
            boolean controllerOnly,
            PermanentPredicate sourceFilter,
            String sourceLabel,
            boolean sourceChosenColor,
            boolean sourceActivationManaColor,
            boolean exileFromLibrary,
            boolean damageRedSourceController) {
        this(scope, gainLife, false, controllerOnly, sourceFilter, sourceLabel, sourceChosenColor,
                false, sourceActivationManaColor, exileFromLibrary, damageRedSourceController, false);
    }

    /** "The next time a source of your choice would deal damage to you this turn, prevent that damage." */
    public static PreventDamageFromChosenSourceEffect nextDamageToYou() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, false, false, null, null, false, false, false, false);
    }

    /** Reverse Damage: as {@link #nextDamageToYou()}, plus "you gain life equal to the damage prevented". */
    public static PreventDamageFromChosenSourceEffect nextDamageToYouAndGainLife() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, true, false, null, null, false, false, false, false);
    }

    /** Deflecting Palm: prevent the damage, then deal it to the chosen source's controller. */
    public static PreventDamageFromChosenSourceEffect nextDamageToYouAndDamageSourceController() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER,
                false, false, false, null, null, false, false, false, false, false, true);
    }

    /**
     * Bone Mask: as {@link #nextDamageToYou()}, plus "exile cards from the top of your library equal
     * to the damage prevented this way".
     */
    public static PreventDamageFromChosenSourceEffect nextDamageToYouAndExileFromLibrary() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, false, false, null, null, false, false, true, false);
    }

    /** Circle of Protection cycle: only sources matching {@code sourceFilter} may be chosen. */
    public static PreventDamageFromChosenSourceEffect nextDamageToYou(PermanentPredicate sourceFilter,
                                                                      String sourceLabel) {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, false, false, sourceFilter, sourceLabel, false, false, false, false);
    }

    /** Prismatic Circle: only sources of the colour chosen for this permanent may be chosen. */
    public static PreventDamageFromChosenSourceEffect nextDamageToYouOfChosenColor() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER, false, false, null, null, true, false, false, false);
    }

    /** Sanctum Guardian: the chosen source's next damage to any target is prevented. */
    public static PreventDamageFromChosenSourceEffect nextDamageToAnyTarget() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_ANY_TARGET, false, false, null, null, false, false, false, false);
    }

    /**
     * Honorable Passage: as {@link #nextDamageToAnyTarget()}, plus "If damage from a red source is
     * prevented this way, Honorable Passage deals that much damage to the source's controller."
     */
    public static PreventDamageFromChosenSourceEffect nextDamageToAnyTargetAndDamageRedSourceController() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_ANY_TARGET, false, false, null, null, false, false, false, true);
    }

    /**
     * Shadowbane: the chosen source's next damage to the controller and/or the creatures they
     * control is prevented; if that source is black, the controller gains that much life.
     */
    public static PreventDamageFromChosenSourceEffect nextDamageToYouAndYourCreatures() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_CONTROLLER_AND_CREATURES, true, false, null, null, false, false, false, false);
    }

    /**
     * Kithkin Armor: the chosen source's next damage to the creature this Aura is attached to is
     * prevented. The Aura is typically sacrificed to pay for the ability, so the attachment is read
     * from the stack entry's last-known source snapshot.
     */
    public static PreventDamageFromChosenSourceEffect nextDamageToEnchantedCreature() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_ENCHANTED, false, false, null, null, false, false, false, false);
    }

    /** Samite Blessing: the chosen source's next damage to target creature is prevented. */
    public static PreventDamageFromChosenSourceEffect nextDamageToTargetCreature() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_TARGET_CREATURE,
                false, false, null, null, false, false, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == ChosenSourcePreventionScope.NEXT_DAMAGE_TO_TARGET_CREATURE
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }

    /** Auriok Replica: prevent all damage the chosen source would deal to you this turn. */
    public static PreventDamageFromChosenSourceEffect allDamageToYou() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, true, null, null, false, false, false, false);
    }

    /** Samite Ministration: prevent all damage from the chosen source to you, gaining life for black or red damage prevented. */
    public static PreventDamageFromChosenSourceEffect allDamageToYouAndGainLifeForBlackOrRedSource() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, true, true, null, null,
                false, false, false, false, false, false);
    }

    /** Protective Sphere: prevent all damage to you from a chosen source matching the activation mana's colour. */
    public static PreventDamageFromChosenSourceEffect allDamageToYouOfActivationManaColor() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, true, null, null, false, true, false, false);
    }

    /** Prevent all damage to you from a chosen source matching the supplied filter. */
    public static PreventDamageFromChosenSourceEffect allDamageToYou(PermanentPredicate sourceFilter,
                                                                     String sourceLabel) {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, true, sourceFilter, sourceLabel, false, false, false, false);
    }

    /** Burrenton Forge-Tender: prevent all damage a matching chosen source would deal this turn. */
    public static PreventDamageFromChosenSourceEffect allDamage(PermanentPredicate sourceFilter,
                                                                String sourceLabel) {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, false, sourceFilter, sourceLabel, false, false, false, false);
    }

    /** Mourner's Shield: prevent all damage from a chosen source sharing a color with the imprinted card. */
    public static PreventDamageFromChosenSourceEffect allDamageFromSourceSharingColorWithImprintedCard() {
        return new PreventDamageFromChosenSourceEffect(
                ChosenSourcePreventionScope.ALL_DAMAGE_THIS_TURN, false, false, false, null, null,
                false, true, false, false, false, false);
    }
}
