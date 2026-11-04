package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the old combat slot collectors reads a triggered ability, now that the ability has a real trigger
 * (effect-slot-refactor.md step 13, family 3). Each constant is named after the slot whose collector it stands for and
 * names the trigger its cards were converted to and the zone the collector reads them in. The collectors in the
 * combat services and {@code TriggerCollectionService} still decide when the ability triggers and how its effects
 * group into stack objects, and its {@link CollectsCombatTrigger} handlers bind the event data, as the slot's did,
 * until step 15 of the refactor replaces them with effects that read the event.
 * <p>
 * Every collector of these watches reads the abilities themselves ({@link Trigger#collectedAsAbility()}), through
 * {@link CombatAbilityReader}: the trigger's filter on the creature, the limit and the multiplicity come from the
 * ability, and the legacy view gives none of them, so {@link #effectsOf} and {@link #registrationsOf} throw.
 */
public enum CombatWatch implements TriggerWatch {
    /** "Whenever this creature attacks". */
    ON_ATTACK(Triggers.attacks(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control attacks", this one included. */
    ON_ALLY_CREATURE_ATTACKS(Triggers.creatureYouControlAttacks(), Zone.BATTLEFIELD),
    /** "Whenever a creature attacks". */
    ON_ANY_CREATURE_ATTACKS(Triggers.creatureAttacks(), Zone.BATTLEFIELD),
    /** "Whenever a creature attacks you or a planeswalker you control". */
    ON_CREATURE_ATTACKS_YOU(Triggers.creatureAttacksYouOrYourPlaneswalker(), Zone.BATTLEFIELD),
    /** "Whenever a creature attacks you". */
    ON_CREATURE_ATTACKS_YOU_DIRECTLY(Triggers.creatureAttacksYou(), Zone.BATTLEFIELD),
    /** "Whenever a creature an opponent controls attacks, if you're the defending player". */
    ON_CREATURE_ATTACKS_DEFENDING_PLAYER(Triggers.creatureAttacksWhileYouDefend(), Zone.BATTLEFIELD),
    /** "Whenever you attack": one or more creatures you control attack. */
    ON_ALLY_CREATURES_ATTACK(Triggers.youAttack(), Zone.BATTLEFIELD),
    /** "Whenever you attack": one or more creatures you control attack, from the command zone. */
    COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK(Triggers.youAttack(), Zone.COMMAND),
    /** "Whenever you attack": one or more creatures you control attack, from the graveyard. */
    GRAVEYARD_ON_ALLY_CREATURES_ATTACK(Triggers.youAttack(), Zone.GRAVEYARD),
    /** "Whenever you attack": one or more creatures you control attack, from exile. */
    EXILE_ON_ALLY_CREATURES_ATTACK(Triggers.youAttack(), Zone.EXILE),
    /** "Whenever you attack a player", once for each player attacked. */
    ON_ALLY_CREATURES_ATTACK_PLAYER(Triggers.youAttackPlayer(), Zone.BATTLEFIELD),
    /** "Whenever you attack a player or planeswalker", once for each one attacked. */
    ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER(Triggers.youAttackPlayerOrPlaneswalker(), Zone.BATTLEFIELD),
    /** "Whenever a player attacks": one or more creatures attack. */
    ON_ANY_PLAYER_ATTACKS(Triggers.playerAttacks(), Zone.BATTLEFIELD),
    /** "Whenever one or more creatures attack you". */
    ON_CREATURES_ATTACK_YOU(Triggers.creaturesAttackYou(), Zone.BATTLEFIELD),
    /** "Whenever an opponent attacks a planeswalker you control with one or more creatures". */
    ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL(Triggers.opponentAttacksYourPlaneswalker(), Zone.BATTLEFIELD),
    /**
     * An attacking creature you control causes a triggered ability of its own to trigger, or this creature enlists a
     * creature.
     */
    ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS(Triggers.attackingCreatureYouControlAbilityTriggers(),
            Zone.BATTLEFIELD),
    /** "Whenever this creature attacks and isn't blocked". */
    ON_ATTACKS_UNBLOCKED(Triggers.attacksUnblocked(), Zone.BATTLEFIELD),
    /** "Whenever enchanted creature attacks and isn't blocked". */
    ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED(Triggers.enchantedCreatureAttacksUnblocked(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control attacks and isn't blocked". */
    ON_ALLY_CREATURE_ATTACKS_UNBLOCKED(Triggers.creatureYouControlAttacksUnblocked(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control attacks and isn't blocked", from exile. */
    EXILE_ON_ALLY_CREATURE_ATTACKS_UNBLOCKED(Triggers.creatureYouControlAttacksUnblocked(), Zone.EXILE),
    /** "Whenever one or more creatures an opponent controls attack you and aren't blocked". */
    ON_OPPONENT_CREATURES_ATTACK_YOU_UNBLOCKED(Triggers.creaturesAttackYouUnblocked(), Zone.BATTLEFIELD),
    /** "Whenever this creature blocks". */
    ON_BLOCK(Triggers.blocks(), Zone.BATTLEFIELD),
    /** "Whenever this creature blocks two or more creatures". */
    ON_BLOCKS_MULTIPLE_CREATURES(Triggers.blocksTwoOrMore(), Zone.BATTLEFIELD),
    /** "Whenever a creature blocks". */
    ON_ANY_CREATURE_BLOCKS(Triggers.creatureBlocks(), Zone.BATTLEFIELD),
    /** "Whenever one or more creatures block". */
    ON_ANY_CREATURES_BLOCK(Triggers.creaturesBlock(), Zone.BATTLEFIELD),
    /** "Whenever this creature becomes blocked". */
    ON_BECOMES_BLOCKED(Triggers.becomesBlocked(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control becomes blocked", this one included. */
    ON_ALLY_CREATURE_BECOMES_BLOCKED(Triggers.creatureYouControlBecomesBlocked(), Zone.BATTLEFIELD),
    /** "Whenever one or more creatures you control become blocked". */
    ON_ALLY_CREATURES_BECOME_BLOCKED(Triggers.creaturesYouControlBecomeBlocked(), Zone.BATTLEFIELD),
    /** "Whenever a creature becomes blocked". */
    ON_ANY_CREATURE_BECOMES_BLOCKED(Triggers.creatureBecomesBlocked(), Zone.BATTLEFIELD),
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
