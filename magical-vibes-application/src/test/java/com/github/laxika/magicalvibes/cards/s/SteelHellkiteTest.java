package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelHellkite.class, DarksteelAxe.class, Forest.class, GrizzlyBears.class,
        LlanowarElves.class, Memnite.class})
class SteelHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("{2}: Steel Hellkite gets +1/+0 until end of turn")
    void pumpAbilityBoostsPower() {
        Permanent hellkite = addReadyHellkite(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump ability can be activated multiple times")
    void pumpAbilityCanBeActivatedMultipleTimes() {
        Permanent hellkite = addReadyHellkite(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys nonland permanents with matching mana value after combat damage")
    void destroysMatchingManaValuePermanentsAfterCombatDamage() {
        Permanent hellkite = addReadyHellkite(player1);

        // Simulate that Steel Hellkite dealt combat damage to player2 this turn
        simulateCombatDamageToPlayer(hellkite, player2);

        // Opponent has a 2-mana creature
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Activate X=2
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X=0 destroys mana value 0 nonland permanents (e.g. Memnite)")
    void xEqualsZeroDestroysManaValueZeroPermanents() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        harness.addToBattlefield(player2, new Memnite());

        // Activate X=0 (costs 0 mana)
        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    @DisplayName("Does not destroy permanents with different mana value")
    void doesNotDestroyDifferentManaValue() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        // LlanowarElves = MV 1, activating with X=2 should not destroy it
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does not destroy lands even if mana value matches")
    void doesNotDestroyLands() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        harness.addToBattlefield(player2, new Forest());

        // Activate X=0 (lands have MV 0)
        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Does not destroy permanents controlled by a player not damaged this turn")
    void doesNotDestroyPermanentsOfUndamagedPlayer() {
        addReadyHellkite(player1);
        // Note: no combat damage dealt to anyone

        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        // Grizzly Bears should survive — Hellkite dealt no combat damage
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy controller's own permanents unless controller was damaged")
    void doesNotDestroyOwnPermanentsUnlessDamaged() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        // Controller's own creature should not be destroyed
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy indestructible permanents")
    void doesNotDestroyIndestructible() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        harness.addToBattlefield(player2, new DarksteelAxe());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Axe");
    }

    @Test
    @DisplayName("X destroy ability can only be activated once per turn")
    void xAbilityOncePerTurn() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        // First activation succeeds
        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        // Second activation should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("End-to-end: attack, deal combat damage, then activate X ability")
    void endToEndCombatDamageAndDestroy() {
        Permanent hellkite = addReadyHellkite(player1);
        hellkite.setAttacking(true);

        // Opponent has a MV 2 creature
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Resolve combat damage
        resolveCombat(player1);

        // Verify combat damage was dealt (player2 should have lost 5 life from 5/5 hellkite)
        harness.assertLife(player2, 15);

        // Verify the tracking was populated
        assertThat(gd.combatDamageToPlayersThisTurn).containsKey(hellkite.getId());
        assertThat(gd.combatDamageToPlayersThisTurn.get(hellkite.getId())).contains(player2.getId());

        // Now activate X=2 to destroy Grizzly Bears
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys multiple permanents with matching mana value")
    void destroysMultipleMatchingPermanents() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        // Two MV 1 creatures on opponent's side
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new Memnite()); // MV 0 - should survive at X=1

        // Add another MV 1 creature
        addCreatureReady(player2, new LlanowarElves());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();

        // Both Llanowar Elves should be destroyed
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        // Memnite (MV 0) should survive
        harness.assertOnBattlefield(player2, "Memnite");
    }

    @Test
    @DisplayName("Destruction includes permanents entering after activation")
    void destroysPermanentsEnteringBeforeResolution() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.addToBattlefield(player2, new Memnite());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    @DisplayName("A second Hellkite does not share the first Hellkite's combat damage")
    void combatDamageTrackingIsPerHellkite() {
        Permanent first = addReadyHellkite(player1);
        addReadyHellkite(player1);
        simulateCombatDamageToPlayer(first, player2);
        harness.addToBattlefield(player2, new Memnite());

        harness.activateAbility(player1, 1, 1, 0, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Memnite");

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Memnite");
    }

    @Test
    @DisplayName("Once-per-turn restriction applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        addReadyHellkite(player1);
        harness.activateAbility(player1, 0, 1, 0, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destruction still resolves after its source leaves the battlefield")
    void destructionResolvesWithoutSourceOnBattlefield() {
        Permanent hellkite = addReadyHellkite(player1);
        simulateCombatDamageToPlayer(hellkite, player2);
        harness.addToBattlefield(player2, new Memnite());

        harness.activateAbility(player1, 0, 1, 0, null);
        gd.playerBattlefields.get(player1.getId()).remove(hellkite);
        gd.playerGraveyards.get(player1.getId()).add(hellkite.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    @DisplayName("Pump, combat damage tracking, and activation limit reset on the next turn")
    void temporaryStateResetsOnNextTurn() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.forceActivePlayer(player1);
        simulateCombatDamageToPlayer(hellkite, player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(hellkite.getPowerModifier()).isEqualTo(1);
        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(hellkite.getPowerModifier()).isZero();

        harness.addToBattlefield(player2, new Memnite());
        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Memnite");
    }

    private Permanent addReadyHellkite(Player player) {
        return addCreatureReady(player, new SteelHellkite());
    }

    private void simulateCombatDamageToPlayer(Permanent source, Player damagedPlayer) {
        Set<java.util.UUID> playerSet = ConcurrentHashMap.newKeySet();
        playerSet.add(damagedPlayer.getId());
        gd.combatDamageToPlayersThisTurn.put(source.getId(), playerSet);
    }
}
