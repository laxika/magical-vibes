package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.c.CorpseCur;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeavyArbalest.class, CarapaceForger.class, Memnite.class, CorpseCur.class, KothOfTheHammer.class})
class HeavyArbalestTest extends BaseCardTest {

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Heavy Arbalest to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent arbalest = addArbalestReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(arbalest.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Granted activated ability: deal 2 damage to creature =====

    @Test
    @DisplayName("Equipped creature can tap to deal 2 damage to target creature")
    void grantedAbilityDeals2DamageToCreature() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        Permanent targetCreature = addCreatureReady(player2, new CarapaceForger());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        // Carapace Forger has 2 toughness without metalcraft, so 2 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(targetCreature.getId()));
        // The equipped creature should be tapped
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Granted activated ability: deal 2 damage to player =====

    @Test
    @DisplayName("Equipped creature can tap to deal 2 damage to a player")
    void grantedAbilityDeals2DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());

        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped =====

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        creature.tap();

        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Doesn't untap during controller's untap step =====

    @Test
    @DisplayName("Equipped creature does not untap during controller's untap step")
    void equippedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        creature.tap();

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(creature.getId());

        // Advance to player2's turn to trigger untap
        advanceToNextTurn(player1);

        // The creature should still be tapped
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped equipped creature remains untapped (doesn't tap it)")
    void untappedEquippedCreatureRemainsUntapped() {
        Permanent creature = addCreatureReady(player2, new CarapaceForger());

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(creature.getId());

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Creature was untapped and stays untapped (effect only prevents untapping)
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other permanents still untap normally when one creature has Heavy Arbalest")
    void otherPermanentsStillUntap() {
        Permanent equippedCreature = addCreatureReady(player2, new CarapaceForger());
        equippedCreature.tap();

        Permanent freeCreature = addCreatureReady(player2, new CarapaceForger());
        freeCreature.tap();

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(equippedCreature.getId());

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Equipped creature stays tapped, free creature untaps
        assertThat(equippedCreature.isTapped()).isTrue();
        assertThat(freeCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature can untap again after Heavy Arbalest is removed")
    void creatureUntapsAfterArbalestRemoved() {
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        creature.tap();

        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(creature.getId());

        // Remove Heavy Arbalest
        gd.playerBattlefields.get(player2.getId()).remove(arbalest);

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Creature should now untap normally
        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Effects stop when equipment is removed =====

    @Test
    @DisplayName("Creature loses granted ability when Heavy Arbalest is removed")
    void creatureLosesAbilityWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());

        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());

        // Remove Heavy Arbalest
        gd.playerBattlefields.get(player1.getId()).remove(arbalest);

        // Creature should no longer have an activated ability
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Equipment itself still untaps =====

    @Test
    @DisplayName("Heavy Arbalest itself still untaps during untap step")
    void arbalestItselfStillUntaps() {
        Permanent arbalest = addArbalestReady(player1);
        arbalest.tap();

        advanceToNextTurn(player2);

        // The equipment itself should untap (the effect only prevents the equipped creature from untapping)
        assertThat(arbalest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted damage ability still resolves after the equipment leaves")
    void activatedAbilityResolvesAfterEquipmentLeaves() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(arbalest);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The equipped creature's infect applies to the granted damage ability")
    void equippedCreatureIsTheDamageSource() {
        Permanent creature = addCreatureReady(player1, new CorpseCur());
        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment controlled by another player grants the creature its ability and untap restriction")
    void differentControllersDoNotDisableEquipmentEffects() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent arbalest = addArbalestReady(player2);
        arbalest.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Reequipping moves both the granted ability and the untap restriction")
    void reequippingMovesBothEffects() {
        Permanent arbalest = addArbalestReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new Memnite());
        Permanent newCreature = addCreatureReady(player1, new Memnite());
        arbalest.setAttachedTo(oldCreature.getId());
        oldCreature.tap();
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        harness.passBothPriorities();

        assertThat(arbalest.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");

        harness.setLife(player2, 20);
        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.performUntapStep(player1);
        assertThat(oldCreature.isTapped()).isFalse();
        assertThat(newCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        addArbalestReady(player1);
        Permanent creature = addCreatureReady(player2, new Memnite());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addArbalestReady(Player player) {
        return addCreatureReady(player, new HeavyArbalest());
    }

    @Test
    @DisplayName("The granted ability deals damage directly to a planeswalker")
    void grantedAbilityDamagesPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent arbalest = addArbalestReady(player1);
        arbalest.setAttachedTo(creature.getId());
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, koth.getId());
        harness.passBothPriorities();

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip requires four mana")
    void equipCannotBeActivatedWithOnlyThreeMana() {
        Permanent arbalest = addArbalestReady(player1);
        Permanent creature = addCreatureReady(player1, new Memnite());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(arbalest.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during the end step")
    void equipRequiresSorceryTiming() {
        Permanent arbalest = addArbalestReady(player1);
        Permanent creature = addCreatureReady(player1, new Memnite());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(arbalest.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
