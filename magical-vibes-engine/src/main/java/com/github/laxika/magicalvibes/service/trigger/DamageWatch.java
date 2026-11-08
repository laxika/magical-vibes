package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the old damage slot collectors reads a triggered ability, now that the ability has a real trigger
 * (effect-slot-refactor.md step 13, family 4). Each constant is named after the slot whose collector it stands for and
 * names the trigger its cards were converted to and the zone the collector reads them in. The collectors in
 * {@code TriggerCollectionService}, {@code DamageSupport} and {@code CombatDamageService} still decide when the
 * ability triggers and how its effects group into stack objects, and its
 * {@link CollectsDamageTrigger} handlers bind the event data, as the slot's did, until step 15 of the refactor
 * replaces them with effects that read the event.
 * <p>
 * Every collector of these watches reads the abilities themselves, through
 * {@link DamageAbilityReader}: the trigger's filter on the permanent, the limit and "once for each source" come from the
 * ability, and the legacy view gives none of them, so {@link #effectsOf} and {@link #registrationsOf} throw.
 */
public enum DamageWatch implements TriggerWatch {
    /** "Whenever this deals damage to a player"; on an Aura or Equipment, the attached creature. */
    ON_DAMAGE_TO_PLAYER(Triggers.dealsDamageToPlayer(), Zone.BATTLEFIELD),
    /** "Whenever this deals damage to an opponent"; on an Aura or Equipment, the attached creature. */
    ON_DAMAGE_TO_OPPONENT(Triggers.dealsDamageToOpponent(), Zone.BATTLEFIELD),
    /** "Whenever a permanent deals damage to you". */
    ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU(Triggers.permanentDealsDamageToYou(), Zone.BATTLEFIELD),
    /** "Whenever enchanted creature is dealt damage". */
    ON_ENCHANTED_CREATURE_DEALT_DAMAGE(Triggers.enchantedCreatureDealtDamage(), Zone.BATTLEFIELD),
    /** "Whenever enchanted player is dealt damage". */
    ON_ENCHANTED_PLAYER_DEALT_DAMAGE(Triggers.enchantedPlayerDealtDamage(), Zone.BATTLEFIELD),
    /** "Whenever enchanted creature deals damage to you". */
    ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU(Triggers.enchantedCreatureDealsDamageToYou(), Zone.BATTLEFIELD),
    /** "Whenever enchanted creature deals damage". */
    ON_ENCHANTED_CREATURE_DEALS_DAMAGE(Triggers.enchantedCreatureDealsDamage(), Zone.BATTLEFIELD),
    /** "Whenever enchanted creature deals damage to a creature". */
    ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE(Triggers.enchantedCreatureDealsDamageToCreature(), Zone.BATTLEFIELD),
    /** "Whenever this permanent is dealt damage". */
    ON_DEALT_DAMAGE(Triggers.dealtDamage(), Zone.BATTLEFIELD),
    /** "Whenever this permanent is dealt noncombat damage". */
    ON_NONCOMBAT_DAMAGE_TO_SELF(Triggers.dealtNoncombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever an opponent is dealt noncombat damage". */
    ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE(Triggers.opponentDealtNoncombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever a creature an opponent controls is dealt damage". */
    ON_OPPONENT_CREATURE_DEALT_DAMAGE(Triggers.creatureOpponentControlsDealtDamage(), Zone.BATTLEFIELD),
    /** "Whenever a creature or planeswalker an opponent controls is dealt excess damage". */
    ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE(
            Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessDamage(), Zone.BATTLEFIELD),
    /** "Whenever a creature or planeswalker an opponent controls is dealt excess noncombat damage". */
    ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE(
            Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessNoncombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever one or more creatures your opponents control are dealt excess noncombat damage". */
    ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE(
            Triggers.creaturesOpponentsControlDealtExcessNoncombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever a creature is dealt damage". */
    ON_ANY_CREATURE_DEALT_DAMAGE(Triggers.creatureDealtDamage(), Zone.BATTLEFIELD),
    /**
     * "Whenever a permanent you control is dealt damage"; the effect can narrow it ("a Dragon you control", "this or a
     * creature it's paired with").
     */
    ON_ANY_PERMANENT_DEALT_DAMAGE(Triggers.permanentYouControlDealtDamage(), Zone.BATTLEFIELD),
    /**
     * "Whenever a [filtered] creature you control deals [combat] damage to a [filtered] creature"; the trigger's filters
     * say which, judged by the collector.
     */
    ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE(Triggers.creatureYouControlDealsDamageToCreature(), Zone.BATTLEFIELD),
    /** "Whenever this creature deals [combat] damage to a [filtered] creature". */
    ON_SELF_DEALS_DAMAGE_TO_CREATURE(Triggers.dealsDamageToCreature(), Zone.BATTLEFIELD),
    /** "Whenever equipped creature deals [combat] damage to a [filtered] creature". */
    ON_EQUIPPED_CREATURE_DEALS_DAMAGE_TO_CREATURE(Triggers.equippedCreatureDealsDamageToCreature(), Zone.BATTLEFIELD),
    /** "Whenever a [filtered] creature deals [combat] damage to a [filtered] creature", whoever controls it. */
    ON_ANY_CREATURE_DEALS_DAMAGE_TO_CREATURE(Triggers.creatureDealsDamageToCreature(), Zone.BATTLEFIELD),
    /**
     * "Whenever an opponent is dealt noncombat damage by a red instant or sorcery spell you control or by a red
     * planeswalker you control", from the graveyard.
     */
    GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER(
            Triggers.opponentDealtDamageByRedInstantSorceryOrPlaneswalkerYouControl(), Zone.GRAVEYARD),
    /**
     * "Whenever an instant or sorcery spell you control deals damage to an opponent or a battle", from the graveyard.
     */
    GRAVEYARD_ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE(
            Triggers.instantOrSorceryYouControlDealsDamageToOpponentOrBattle(), Zone.GRAVEYARD),
    /** "Whenever a source you control deals noncombat damage to an opponent", from the graveyard. */
    GRAVEYARD_ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT(
            Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), Zone.GRAVEYARD),
    /** "Whenever a source deals damage"; the trigger's filters say which source ("a red creature or spell"). */
    ON_ANY_SOURCE_DEALS_DAMAGE(Triggers.sourceDealsDamage(), Zone.BATTLEFIELD),
    /** "Whenever a source you control deals damage"; the trigger's filters say which source ("a red source"). */
    ON_ALLY_SOURCE_DEALS_DAMAGE(Triggers.sourceYouControlDealsDamage(), Zone.BATTLEFIELD),
    /** "Whenever an instant or sorcery spell you control deals damage". */
    ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE(Triggers.instantOrSorceryYouControlDealsDamage(), Zone.BATTLEFIELD),
    /** "Whenever an instant or sorcery spell you control deals damage to an opponent", once for each opponent. */
    ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT(Triggers.instantOrSorceryYouControlDealsDamageToOpponent(),
            Zone.BATTLEFIELD),
    /** "Whenever this deals damage". */
    ON_SELF_DEALS_DAMAGE(Triggers.dealsDamage(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control deals damage to a planeswalker". */
    ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER(
            Triggers.creatureYouControlDealsDamageToPlaneswalker(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control deals combat damage to a planeswalker". */
    ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLANESWALKER(
            Triggers.creatureYouControlDealsCombatDamageToPlaneswalker(), Zone.BATTLEFIELD),
    /** "Whenever a creature deals damage to enchanted planeswalker". */
    ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER(
            Triggers.creatureDealsDamageToEnchantedPlaneswalker(), Zone.BATTLEFIELD),
    /** "Whenever you're dealt damage". */
    ON_CONTROLLER_DEALT_DAMAGE(Triggers.youAreDealtDamage(), Zone.BATTLEFIELD),
    /** "Whenever a source you control deals damage to you". */
    ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE(Triggers.youAreDealtDamageBySourceYouControl(), Zone.BATTLEFIELD),
    /** "Whenever damage that would be dealt to you is prevented". */
    ON_CONTROLLER_DAMAGE_PREVENTED(Triggers.damageToYouPrevented(), Zone.BATTLEFIELD),
    /** "Whenever an opponent is dealt damage". */
    ON_OPPONENT_DEALT_DAMAGE(Triggers.opponentDealtDamage(), Zone.BATTLEFIELD),
    /** "Whenever a source you control deals damage to an opponent". */
    ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT(Triggers.sourceYouControlDealsDamageToOpponent(), Zone.BATTLEFIELD),
    /** "Whenever a source you control deals damage to a player". */
    ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER(Triggers.sourceYouControlDealsDamageToPlayer(), Zone.BATTLEFIELD),
    /**
     * "Whenever a creature deals damage to its controller", on a plane: a creature whose controller isn't the planar
     * controller.
     */
    ON_SOURCE_DEALS_DAMAGE_TO_ITS_CONTROLLER_PLANAR(Triggers.creatureDealsDamageToItsController(), Zone.BATTLEFIELD),
    /** "Whenever one or more creatures you control deal damage to a player". */
    ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER(Triggers.creaturesYouControlDealDamageToPlayer(), Zone.BATTLEFIELD),
    /** "Whenever one or more creatures you control deal damage to an opponent". */
    ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT(Triggers.creaturesYouControlDealDamageToOpponent(), Zone.BATTLEFIELD),
    /** "Whenever a source you control deals noncombat damage to an opponent". */
    ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT(
            Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), Zone.BATTLEFIELD),
    /** "Whenever a source an opponent controls deals damage to you". */
    ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT(Triggers.youAreDealtDamageBySourceOpponentControls(), Zone.BATTLEFIELD),
    /** "Whenever a source an opponent controls deals damage to you or a permanent you control". */
    ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT(
            Triggers.sourceOpponentControlsDealsDamageToYouOrYourPermanent(), Zone.BATTLEFIELD),
    /**
     * "Whenever a creature deals damage to you or a permanent you control"; the trigger's filter narrows the creature
     * ("of the chosen color") and its damaged filter the permanent ("a white creature you control").
     */
    ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT(
            Triggers.creatureDealsDamageToYouOrYourPermanent(), Zone.BATTLEFIELD),
    /** "Whenever a source you control deals noncombat damage to a creature". */
    ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE(
            Triggers.sourceYouControlDealsNoncombatDamageToCreature(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    DamageWatch(Trigger trigger, Zone zone) {
        this.trigger = trigger;
        this.zone = zone;
    }

    @Override
    public Trigger trigger() {
        return trigger;
    }

    @Override
    public Zone zone() {
        return zone;
    }

    /** Throws: the collectors of a damage watch read its abilities themselves ({@link DamageAbilityReader}). */
    @Override
    public List<CardEffect> effectsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }

    /** Throws: the collectors of a damage watch read its abilities themselves ({@link DamageAbilityReader}). */
    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }
}
