package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.cards.w.WillForgedGolem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConeOfFlame.class, ChandraPyromaster.class, RuneclawBear.class, SiegeWurm.class,
        WillForgedGolem.class, Mountain.class})
class ConeOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cone of Flame with 3 creature targets puts it on the stack")
    void castingWithThreeCreatureTargetsPutsOnStack() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.addToBattlefield(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();
        UUID id3 = bf.get(2).getId();

        harness.castSorcery(player1, 0, List.of(id1, id2, id3));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetIds()).containsExactly(id1, id2, id3);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.addToBattlefield(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 3);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast with fewer than 3 targets")
    void cannotCastWithFewerThanThreeTargets() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(bf.get(0).getId(), bf.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot cast with duplicate targets")
    void cannotCastWithDuplicateTargets() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(id1, id2, id1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Deals ordered damage: 1 to first, 2 to second, 3 to third creature")
    void dealsOrderedDamageToCreatures() {
        // Runeclaw Bear (2/2): 1 damage → survives, 2 damage → dies, 3 damage → dies
        // Will-Forged Golem (4/4): survives 1, 2, or 3 damage
        // Siege Wurm (5/5): survives 1, 2, or 3 damage
        harness.addToBattlefield(player2, new WillForgedGolem()); // Target 1: 1 damage (survives, 4 toughness)
        harness.addToBattlefield(player2, new RuneclawBear());    // Target 2: 2 damage (dies, 2 toughness)
        harness.addToBattlefield(player2, new SiegeWurm());       // Target 3: 3 damage (survives, 5 toughness)
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID golemId = bf.get(0).getId();
        UUID bearId = bf.get(1).getId();
        UUID wurmId = bf.get(2).getId();

        harness.castAndResolveSorcery(player1, 0, List.of(golemId, bearId, wurmId));

        // Will-Forged Golem took 1 damage (survives: 1 < 4 toughness)
        harness.assertOnBattlefield(player2, "Will-Forged Golem");

        // Runeclaw Bear took 2 damage (dies: 2 >= 2 toughness)
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");

        // Siege Wurm took 3 damage (survives: 3 < 5 toughness)
        harness.assertOnBattlefield(player2, "Siege Wurm");
    }

    @Test
    @DisplayName("3 damage to third target kills a 2/2 creature")
    void thirdTargetThreeDamageKillsSmallCreature() {
        harness.addToBattlefield(player2, new WillForgedGolem()); // Target 1: 1 damage (survives)
        harness.addToBattlefield(player2, new SiegeWurm());       // Target 2: 2 damage (survives)
        harness.addToBattlefield(player2, new RuneclawBear());    // Target 3: 3 damage (dies)
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castAndResolveSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId()));

        harness.assertOnBattlefield(player2, "Will-Forged Golem");
        harness.assertOnBattlefield(player2, "Siege Wurm");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Deals ordered damage to both players and a creature")
    void dealsOrderedDamageToPlayers() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearId = harness.getPermanentId(player2, "Runeclaw Bear");

        // Target 1 (1 dmg): player2, Target 2 (2 dmg): creature, Target 3 (3 dmg): player1
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), bearId, player1.getId()));

        // Player 2 took 1 damage
        harness.assertLife(player2, 19);
        // Player 1 took 3 damage
        harness.assertLife(player1, 17);
        // Runeclaw Bear took 2 damage (dies: 2 >= 2)
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Can target both players and a creature")
    void canTargetBothPlayersAndCreature() {
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID golemId = harness.getPermanentId(player2, "Will-Forged Golem");

        // Target 1 (1 dmg): player1, Target 2 (2 dmg): player2, Target 3 (3 dmg): creature
        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId(), golemId));

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        // Will-Forged Golem took 3 damage (survives: 3 < 4)
        harness.assertOnBattlefield(player2, "Will-Forged Golem");
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.addToBattlefield(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID golemId = harness.getPermanentId(player2, "Will-Forged Golem");
        UUID wurmId = harness.getPermanentId(player2, "Siege Wurm");

        harness.castAndResolveSorcery(player1, 0, List.of(chandra.getId(), golemId, wurmId));

        Permanent remainingChandra = findPermanent(player2, "Chandra, Pyromaster");
        assertThat(remainingChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target own creatures")
    void canTargetOwnCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new WillForgedGolem());
        harness.addToBattlefield(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID ownBearId = harness.getPermanentId(player1, "Runeclaw Bear");
        UUID ownGolemId = harness.getPermanentId(player1, "Will-Forged Golem");
        UUID oppWurmId = harness.getPermanentId(player2, "Siege Wurm");

        harness.castAndResolveSorcery(player1, 0, List.of(ownBearId, ownGolemId, oppWurmId));

        // Own Runeclaw Bear took 1 damage (survives: 1 < 2)
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        // Own Will-Forged Golem took 2 damage (survives: 2 < 4)
        harness.assertOnBattlefield(player1, "Will-Forged Golem");
        // Opponent Siege Wurm took 3 damage (survives: 3 < 5)
        harness.assertOnBattlefield(player2, "Siege Wurm");
    }

    @Test
    @DisplayName("Partially resolves when one creature target is removed")
    void partiallyResolvesWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID bearId = bf.get(0).getId();
        UUID golemId = bf.get(1).getId();

        // Targets: bear (1 dmg), golem (2 dmg), player2 (3 dmg)
        harness.castSorcery(player1, 0, List.of(bearId, golemId, player2.getId()));

        // Remove the first target (bear) before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).removeFirst();

        harness.passBothPriorities();

        // Bear was removed before resolution — skipped
        // Will-Forged Golem took 2 damage (survives: 2 < 4)
        harness.assertOnBattlefield(player2, "Will-Forged Golem");
        // Player 2 took 3 damage
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Still deals damage to remaining targets when all creature targets removed")
    void stillDamagesPlayersWhenCreatureTargetsRemoved() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearId = harness.getPermanentId(player2, "Runeclaw Bear");

        // Targets: bear (1 dmg), player1 (2 dmg), player2 (3 dmg)
        harness.castSorcery(player1, 0, List.of(bearId, player1.getId(), player2.getId()));

        // Remove the creature before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Bear gone, damage skipped
        // Player 1 took 2 damage
        harness.assertLife(player1, 18);
        // Player 2 took 3 damage
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.addToBattlefield(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castAndResolveSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId()));

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cone of Flame goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new WillForgedGolem());
        harness.addToBattlefield(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castAndResolveSorcery(player1, 0, List.of(bf.get(0).getId(), bf.get(1).getId(), bf.get(2).getId()));

        harness.assertInGraveyard(player1, "Cone of Flame");
    }

    @Test
    @DisplayName("Marks exactly 1, 2, and 3 damage on the ordered targets")
    void marksExactOrderedDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WillForgedGolem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new WillForgedGolem());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        assertThat(third.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing the middle target preserves damage to the first and third targets")
    void removingMiddleTargetPreservesDamageAmounts() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WillForgedGolem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(third.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Cone of Flame");
    }

    @Test
    @DisplayName("Does not resolve when all three targets have left the battlefield")
    void allTargetsRemoved() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WillForgedGolem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(third.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cone of Flame");
    }

    @Test
    @DisplayName("Cannot choose more than three targets")
    void cannotCastWithFourTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WillForgedGolem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature land")
    void cannotTargetNoncreatureLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), player2.getId(), mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Can target a battle and remove defense counters")
    void canTargetBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of(battle.getId(), player1.getId(), player2.getId()));

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }
}

