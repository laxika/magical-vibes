package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the combat damage collectors reads a triggered ability ("whenever this creature deals combat damage to a
 * player" and the like, CR 510.2). Each constant names the trigger its cards are written with and the zone the
 * collector reads them in; the collectors in {@code CombatDamageService} and {@code TriggerCollectionService} decide
 * when the ability triggers. The attack and block abilities are the trigger pipeline's ({@code AttackEvent},
 * {@code BlockEvent}).
 * <p>
 * Every collector of these watches reads the abilities themselves, through
 * {@link CombatAbilityReader}: the trigger's filter on the creature, the limit and the multiplicity come from the
 * ability, and the legacy view gives none of them, so {@link #effectsOf} and {@link #registrationsOf} throw.
 */
public enum CombatWatch implements TriggerWatch {
    /** "Whenever this creature deals combat damage to a player". */
    ON_COMBAT_DAMAGE_TO_PLAYER(Triggers.dealsCombatDamageToPlayer(), Zone.BATTLEFIELD),
    /** "Whenever equipped creature deals combat damage to a player", once each combat damage step. */
    ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
            Zone.BATTLEFIELD),
    /** "Whenever a creature you control deals combat damage to a player". */
    ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER(Triggers.creatureYouControlDealsCombatDamageToPlayer(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control deals combat damage to a player", from the graveyard. */
    GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
            Zone.GRAVEYARD),
    /** "Whenever a creature deals combat damage to a player". */
    ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER(Triggers.creatureDealsCombatDamageToPlayer(), Zone.BATTLEFIELD),
    /** "Whenever a creature with the same name as this deals combat damage to a player". */
    ON_CREATURE_WITH_SAME_NAME_COMBAT_DAMAGE_TO_PLAYER(Triggers.creatureWithSameNameDealsCombatDamageToPlayer(),
            Zone.BATTLEFIELD),
    /** "Whenever a creature deals combat damage to one of your opponents". */
    ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT(Triggers.creatureDealsCombatDamageToOpponent(), Zone.BATTLEFIELD),
    /** "Whenever one or more goaded creatures deal combat damage to one of your opponents". */
    ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT(Triggers.goadedCreaturesDealCombatDamageToOpponent(),
            Zone.BATTLEFIELD),
    /** "Whenever a creature deals combat damage to its owner". */
    ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER(Triggers.creatureDealsCombatDamageToItsOwner(), Zone.BATTLEFIELD),
    /** "Whenever this creature deals combat damage to a creature", once for each creature. */
    ON_COMBAT_DAMAGE_TO_CREATURE(Triggers.dealsCombatDamageToCreature(), Zone.BATTLEFIELD),
    /** "Whenever this creature deals combat damage to one or more blocking creatures". */
    ON_COMBAT_DAMAGE_TO_BLOCKING_CREATURE(Triggers.dealsCombatDamageToBlockingCreature(), Zone.BATTLEFIELD),
    /** "Whenever this creature deals combat damage", once each combat damage step. */
    ON_SELF_DEALS_COMBAT_DAMAGE(Triggers.dealsCombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever equipped creature deals combat damage", once each combat damage step. */
    ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE(Triggers.equippedCreatureDealsCombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control deals combat damage", once for each creature each combat damage step. */
    ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE(Triggers.creatureYouControlDealsCombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever this creature deals combat damage to a player or planeswalker". */
    ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER(Triggers.dealsCombatDamageToPlayerOrPlaneswalker(),
            Zone.BATTLEFIELD),
    /** "Whenever a creature you control deals combat damage to a player or planeswalker", from the graveyard. */
    GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER(
            Triggers.creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker(), Zone.GRAVEYARD),
    /** "Whenever this creature deals combat damage to a player or battle". */
    ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE(Triggers.dealsCombatDamageToPlayerOrBattle(), Zone.BATTLEFIELD),
    /** "Whenever equipped creature deals combat damage to a player or battle". */
    ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE(
            Triggers.equippedCreatureDealsCombatDamageToPlayerOrBattle(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control deals combat damage to a player or battle". */
    ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE(Triggers.creatureYouControlDealsCombatDamageToPlayerOrBattle(),
            Zone.BATTLEFIELD),
    /** "Whenever a creature deals combat damage to you". */
    ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU(Triggers.creatureDealsCombatDamageToYou(), Zone.BATTLEFIELD),
    /** "Whenever this permanent is dealt combat damage". */
    ON_COMBAT_DAMAGE_TO_SELF(Triggers.dealtCombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever combat damage is dealt to you". */
    ON_CONTROLLER_DEALT_COMBAT_DAMAGE(Triggers.youAreDealtCombatDamage(), Zone.BATTLEFIELD),
    /** "Whenever combat damage is dealt to you or a planeswalker you control", from the graveyard. */
    GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER(Triggers.youOrYourPlaneswalkerDealtCombatDamage(),
            Zone.GRAVEYARD);

    private final Trigger trigger;
    private final Zone zone;

    CombatWatch(Trigger trigger, Zone zone) {
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

    /** Throws: the collectors of a combat watch read its abilities themselves ({@link CombatAbilityReader}). */
    @Override
    public List<CardEffect> effectsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }

    /** Throws: the collectors of a combat watch read its abilities themselves ({@link CombatAbilityReader}). */
    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }
}
