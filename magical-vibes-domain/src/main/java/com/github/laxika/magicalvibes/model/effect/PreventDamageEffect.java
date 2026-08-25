package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * One-shot damage-prevention spell/ability: writes the shield state selected by {@link #scope()}.
 * Collapses the shield-creating {@code Prevent*Effect} family — every "prevent the next N damage
 * to <victim>" and "prevent all [combat] damage to/by <victim> this turn" wording is this record
 * with a scope, never a new effect class. Build instances via the static factories.
 *
 * <p>The consumption side (how shields absorb damage) lives unchanged in
 * {@code DamagePreventionService}; static always-on prevention markers with riders (Vigor, Purity,
 * Urza's Armor, CoP-style chosen-source shields, …) remain separate records.
 *
 * @param scope           which shield-state slot to write
 * @param amount          the shield size for {@code NEXT_*} scopes ({@code null} for ALL-style scopes)
     * @param combatOnly      combat-only window for the controller, target-creature, and matching-permanent scopes
 * @param sourceColors    the prevented source colors for color-based prevention scopes
 * @param exemptPredicate creatures still dealing combat damage for {@link PreventionScope#ALL_COMBAT_EXCEPT}
     * @param victimPredicate permanents all damage to which is prevented for {@link PreventionScope#ALL_TO_MATCHING_PERMANENTS}
     *                        or all combat damage to which is prevented for
     *                        {@link PreventionScope#ALL_COMBAT_TO_CONTROLLED_MATCHING_PERMANENTS};
 *                        for {@link PreventionScope#NEXT_TO_TARGET_CREATURE} an optional narrowing of the legal target
 * @param gainLife        whether the controller gains life equal to damage prevented by a
 *                        {@link PreventionScope#NEXT_TO_TARGET} shield
 * @param sourcePredicate damage sources matching this predicate for
 *                        {@link PreventionScope#ALL_TO_CONTROLLER_FROM_MATCHING_SOURCES} or
 *                        {@link PreventionScope#ALL_TO_PLAYERS_FROM_MATCHING_SOURCES}
 */
public record PreventDamageEffect(
        PreventionScope scope,
        DynamicAmount amount,
        boolean combatOnly,
        Set<CardColor> sourceColors,
        PermanentPredicate exemptPredicate,
        PermanentPredicate victimPredicate,
        boolean gainLife,
        PermanentPredicate sourcePredicate
) implements CardEffect {

    public PreventDamageEffect(PreventionScope scope,
                               DynamicAmount amount,
                               boolean combatOnly,
                               Set<CardColor> sourceColors,
                               PermanentPredicate exemptPredicate,
                               PermanentPredicate victimPredicate) {
        this(scope, amount, combatOnly, sourceColors, exemptPredicate, victimPredicate, false, null);
    }

    public PreventDamageEffect(PreventionScope scope,
                               DynamicAmount amount,
                               boolean combatOnly,
                               Set<CardColor> sourceColors,
                               PermanentPredicate exemptPredicate,
                               PermanentPredicate victimPredicate,
                               boolean gainLife) {
        this(scope, amount, combatOnly, sourceColors, exemptPredicate, victimPredicate, gainLife, null);
    }

    public PreventDamageEffect {
        boolean needsAmount = scope == PreventionScope.NEXT_TO_ANY
                || scope == PreventionScope.NEXT_TO_CONTROLLER
                || scope == PreventionScope.NEXT_TO_SELF
                || scope == PreventionScope.NEXT_TO_ENCHANTED
                || scope == PreventionScope.NEXT_TO_TARGET
                || scope == PreventionScope.NEXT_TO_TARGET_CREATURE
                || scope == PreventionScope.NEXT_TO_TARGET_PLAYER_OR_PLANESWALKER
                || scope == PreventionScope.NEXT_TO_EACH_CREATURE_AND_PLAYER;
        if (needsAmount && amount == null) {
            throw new IllegalArgumentException("NEXT_* prevention scopes require an amount: " + scope);
        }
        if (!needsAmount && amount != null) {
            throw new IllegalArgumentException("ALL-style prevention scopes take no amount: " + scope);
        }
        boolean colorSourceScope = scope == PreventionScope.ALL_FROM_COLORS
                || scope == PreventionScope.ALL_FROM_COLORS_TO_CONTROLLED_CREATURES;
        if ((sourceColors != null) != colorSourceScope) {
            throw new IllegalArgumentException("sourceColors is exactly a color-based prevention parameter: " + scope);
        }
        if ((exemptPredicate != null) != (scope == PreventionScope.ALL_COMBAT_EXCEPT)) {
            throw new IllegalArgumentException("exemptPredicate is exactly the ALL_COMBAT_EXCEPT parameter: " + scope);
        }
        boolean acceptsVictimPredicate = scope == PreventionScope.ALL_TO_MATCHING_PERMANENTS
                || scope == PreventionScope.ALL_COMBAT_TO_CONTROLLED_MATCHING_PERMANENTS
                || scope == PreventionScope.NEXT_TO_TARGET_CREATURE;
        if (victimPredicate != null && !acceptsVictimPredicate) {
            throw new IllegalArgumentException(
                    "victimPredicate is only a matching-permanent or NEXT_TO_TARGET_CREATURE parameter: " + scope);
        }
        if (victimPredicate == null && (scope == PreventionScope.ALL_TO_MATCHING_PERMANENTS
                || scope == PreventionScope.ALL_COMBAT_TO_CONTROLLED_MATCHING_PERMANENTS)) {
            throw new IllegalArgumentException(scope + " requires a victimPredicate");
        }
        if (gainLife && scope != PreventionScope.NEXT_TO_TARGET) {
            throw new IllegalArgumentException("gainLife is exactly the NEXT_TO_TARGET parameter: " + scope);
        }
        boolean acceptsSourcePredicate = scope == PreventionScope.ALL_TO_CONTROLLER_FROM_MATCHING_SOURCES
                || scope == PreventionScope.ALL_TO_PLAYERS_FROM_MATCHING_SOURCES;
        if ((sourcePredicate != null) != acceptsSourcePredicate) {
            throw new IllegalArgumentException(
                    "sourcePredicate is exactly a matching-source prevention parameter: " + scope);
        }
    }

    /** "Prevent the next {@code amount} damage that would be dealt to any permanent or player." */
    public static PreventDamageEffect nextToAny(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_ANY, new Fixed(amount), false, null, null, null);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to you." */
    public static PreventDamageEffect nextToController(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_CONTROLLER, new Fixed(amount), false, null, null, null);
    }

    /** "Prevent the next {@code amount} combat damage that would be dealt to you." */
    public static PreventDamageEffect nextCombatToController(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_CONTROLLER, new Fixed(amount), true, null, null, null);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to ~." */
    public static PreventDamageEffect nextToSelf(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_SELF, new Fixed(amount), false, null, null, null);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to enchanted creature this turn" (Fylgja). */
    public static PreventDamageEffect nextToEnchanted(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_ENCHANTED, new Fixed(amount), false, null, null, null);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to any target." */
    public static PreventDamageEffect nextToTarget(int amount) {
        return nextToTarget(new Fixed(amount));
    }

    /** "Prevent the next X damage that would be dealt to any target" (Alabaster Potion). */
    public static PreventDamageEffect nextToTarget(DynamicAmount amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_TARGET, amount, false, null, null, null);
    }

    /** "Prevent the next {@code amount} damage to any target; you gain life equal to damage prevented this way." */
    public static PreventDamageEffect nextToTargetAndGainLife(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_TARGET, new Fixed(amount), false, null, null, null, true);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to target creature" (Soldevi Heretic). */
    public static PreventDamageEffect nextToTargetCreature(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_TARGET_CREATURE, new Fixed(amount), false, null, null, null);
    }

    /**
     * "Prevent the next {@code amount} damage that would be dealt to target [restricted] creature"
     * (Eiganjo Castle — target legendary creature). {@code victimPredicate} narrows the creature-only
     * {@code TargetSpec} so an illegal creature can never be chosen.
     */
    public static PreventDamageEffect nextToTargetCreature(int amount, PermanentPredicate victimPredicate) {
        return new PreventDamageEffect(
                PreventionScope.NEXT_TO_TARGET_CREATURE, new Fixed(amount), false, null, null, victimPredicate);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to target player or planeswalker" (Wandering Mage). */
    public static PreventDamageEffect nextToTargetPlayerOrPlaneswalker(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_TARGET_PLAYER_OR_PLANESWALKER, new Fixed(amount), false, null, null, null);
    }

    /**
     * "Prevent the next {@code amount} damage that would be dealt to each creature and each player
     * this turn" (Kitsune Palliator). Non-targeting; only creatures on the battlefield as the
     * ability resolves are shielded.
     */
    public static PreventDamageEffect nextToEachCreatureAndPlayer(int amount) {
        return new PreventDamageEffect(
                PreventionScope.NEXT_TO_EACH_CREATURE_AND_PLAYER, new Fixed(amount), false, null, null, null);
    }

    /** "Prevent all combat damage that would be dealt this turn." */
    public static PreventDamageEffect allCombat() {
        return new PreventDamageEffect(PreventionScope.ALL_COMBAT, null, false, null, null, null);
    }

    /** "Prevent all combat damage that would be dealt this turn by attacking creatures" (Harmless Assault). */
    public static PreventDamageEffect allCombatByAttackingCreatures() {
        return new PreventDamageEffect(
                PreventionScope.ALL_COMBAT_BY_ATTACKING_CREATURES, null, false, null, null, null);
    }

    /** "Prevent all combat damage that would be dealt to players this turn" (Defend the Hearth). */
    public static PreventDamageEffect allCombatToPlayers() {
        return new PreventDamageEffect(PreventionScope.ALL_COMBAT_TO_PLAYERS, null, false, null, null, null);
    }

    /** "Prevent all damage that would be dealt to creatures this turn." */
    public static PreventDamageEffect allToCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_CREATURES, null, false, null, null, null);
    }

    /** "Prevent all damage that would be dealt to creatures you control this turn" (Divine Light). */
    public static PreventDamageEffect allToControlledCreatures() {
        return new PreventDamageEffect(
                PreventionScope.ALL_TO_CONTROLLED_CREATURES, null, false, null, null, null);
    }

    /** "Prevent all damage that would be dealt to [permanents matching {@code victimPredicate}] this turn" (Ethersworn Shieldmage). */
    public static PreventDamageEffect allToMatchingPermanents(PermanentPredicate victimPredicate) {
        return new PreventDamageEffect(PreventionScope.ALL_TO_MATCHING_PERMANENTS, null, false, null, null, victimPredicate);
    }

    /** "Prevent all combat damage that would be dealt this turn to matching permanents you control." */
    public static PreventDamageEffect allCombatToControlledMatchingPermanents(PermanentPredicate victimPredicate) {
        return new PreventDamageEffect(
                PreventionScope.ALL_COMBAT_TO_CONTROLLED_MATCHING_PERMANENTS, null, true, null, null, victimPredicate);
    }

    /** "Prevent all damage that would be dealt to target creature(s) this turn." */
    public static PreventDamageEffect allToTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_TARGET_CREATURES, null, false, null, null, null);
    }

    /** "Prevent all combat damage that would be dealt to target creature(s) this turn" (Foxfire). */
    public static PreventDamageEffect allCombatToTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_TARGET_CREATURES, null, true, null, null, null);
    }

    /** "Prevent all damage target creature(s) would deal this turn" (Soul Parry). */
    public static PreventDamageEffect allByTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_TARGET_CREATURES, null, false, null, null, null);
    }

    /** "Prevent all combat damage target creature(s) would deal this turn" (Foxfire, Inquisitor's Snare). */
    public static PreventDamageEffect allCombatByTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_TARGET_CREATURES, null, true, null, null, null);
    }

    /** "Prevent all damage that would be dealt by creatures this turn" (Ethereal Haze). */
    public static PreventDamageEffect allByCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_CREATURES, null, false, null, null, null);
    }

    /** "Until your next turn, prevent all damage target permanent would deal" (Gideon of the Trials +1). */
    public static PreventDamageEffect allByTargetPermanentUntilNextTurn() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN, null, false, null, null, null);
    }

    /** "Until your next turn, prevent all damage that would be dealt to and dealt by target permanent." */
    public static PreventDamageEffect allToAndByTargetPermanentUntilNextTurn() {
        return new PreventDamageEffect(
                PreventionScope.ALL_TO_AND_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN,
                null, false, null, null, null);
    }

    /** "Prevent all damage that would be dealt to ~ this turn" — the source permanent (Gideon of the Trials 0). */
    public static PreventDamageEffect allToSelf() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_SELF, null, false, null, null, null);
    }

    /** "Prevent all combat damage that would be dealt to ~ this turn" — the source permanent (Oketra's Avenger). */
    public static PreventDamageEffect allCombatToSelf() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_SELF, null, true, null, null, null);
    }

    /** "Prevent all combat damage that would be dealt by ~ this turn" — the source permanent (Goblin Snowman). */
    public static PreventDamageEffect allCombatBySelf() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_SELF, null, true, null, null, null);
    }

    /** "Prevent all damage that would be dealt to you and creatures you control this turn." */
    public static PreventDamageEffect allToControllerAndCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES, null, false, null, null, null);
    }

    /** "Prevent all damage that would be dealt to you this turn" (Riot Control) — the player only. */
    public static PreventDamageEffect allToController() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_CONTROLLER, null, false, null, null, null);
    }

    /** "Until your next turn, prevent all damage that would be dealt to you" (Morningtide's Light). */
    public static PreventDamageEffect allToControllerUntilNextTurn() {
        return new PreventDamageEffect(
                PreventionScope.ALL_TO_CONTROLLER_UNTIL_NEXT_TURN, null, false, null, null, null);
    }

    /** "Prevent all damage attacking creatures would deal to you this turn" (Deep Wood). */
    public static PreventDamageEffect allToControllerFromAttackers() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_CONTROLLER_FROM_ATTACKERS, null, false, null, null, null);
    }

    /** "Prevent all damage that would be dealt to you this turn by sources matching {@code sourcePredicate}." */
    public static PreventDamageEffect allToControllerFromMatchingSources(PermanentPredicate sourcePredicate) {
        return new PreventDamageEffect(
                PreventionScope.ALL_TO_CONTROLLER_FROM_MATCHING_SOURCES,
                null, false, null, null, null, false, sourcePredicate);
    }

    /** "Prevent all damage that would be dealt to players this turn by matching sources." */
    public static PreventDamageEffect allToPlayersFromMatchingSources(PermanentPredicate sourcePredicate) {
        return new PreventDamageEffect(
                PreventionScope.ALL_TO_PLAYERS_FROM_MATCHING_SOURCES,
                null, false, null, null, null, false, sourcePredicate);
    }

    /** "Prevent all damage that sources of the given colors would deal this turn" (Luminesce). */
    public static PreventDamageEffect fromColors(Set<CardColor> colors) {
        return new PreventDamageEffect(PreventionScope.ALL_FROM_COLORS, null, false, colors, null, null);
    }

    /** "Prevent all damage that black and/or red sources would deal to creatures you control this turn." */
    public static PreventDamageEffect fromColorsToControlledCreatures(Set<CardColor> colors) {
        return new PreventDamageEffect(
                PreventionScope.ALL_FROM_COLORS_TO_CONTROLLED_CREATURES, null, false, colors, null, null);
    }

    /** "Prevent all damage that sources of the color of your choice would deal this turn" (Prismatic Strands). */
    public static PreventDamageEffect fromChosenColor() {
        return new PreventDamageEffect(PreventionScope.ALL_FROM_CHOSEN_COLOR, null, false, null, null, null);
    }

    /** "Prevent all damage that would be dealt this turn by non-Human sources" (Repel the Abominable). */
    public static PreventDamageEffect fromNonHumanSources() {
        return new PreventDamageEffect(PreventionScope.ALL_FROM_NON_HUMAN_SOURCES, null, false, null, null, null);
    }

    /** "Prevent all combat damage this turn except that dealt by matching creatures" (Moonmist). */
    public static PreventDamageEffect allCombatExcept(PermanentPredicate exemptPredicate) {
        return new PreventDamageEffect(PreventionScope.ALL_COMBAT_EXCEPT, null, false, null, exemptPredicate, null);
    }

    /**
     * "Prevent all combat damage that would be dealt by creatures other than target creature this
     * turn" (Terrifying Presence). The exemption is the chosen target, resolved on resolution.
     */
    public static PreventDamageEffect allCombatExceptTargetCreature() {
        return new PreventDamageEffect(PreventionScope.ALL_COMBAT_EXCEPT_TARGET, null, false, null, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case NEXT_TO_TARGET -> TargetSpec.benign(TargetPredicates.anyTarget());
            case ALL_COMBAT_EXCEPT_TARGET -> TargetSpec.benign(TargetPredicates.creature());
            case NEXT_TO_TARGET_CREATURE -> TargetSpec.benign(victimPredicate == null
                    ? TargetPredicates.creature()
                    : TargetPredicates.narrowPermanents(TargetPredicates.creature(), victimPredicate));
            case NEXT_TO_TARGET_PLAYER_OR_PLANESWALKER -> TargetSpec.benign(TargetPredicates.playerOrPlaneswalker());
            case ALL_TO_TARGET_CREATURES, ALL_BY_TARGET_CREATURES -> TargetSpec.benign(TargetPredicates.creature());
            case ALL_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN,
                 ALL_TO_AND_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
