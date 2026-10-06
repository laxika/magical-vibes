package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GlintHawk;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.c.ChimericMass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RatchetBomb.class, DarksteelAxe.class, Forest.class, GoldMyr.class,
        GlintHawk.class, Memnite.class, Arrest.class, ChimericMass.class})
class RatchetBombTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Ratchet Bomb puts a charge counter on it")
    void tappingAddsChargeCounter() {
        Permanent bomb = addReadyBomb(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations accumulate charge counters")
    void multipleActivationsAccumulateCounters() {
        Permanent bomb = addReadyBomb(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        bomb.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        bomb.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability 0 when already tapped")
    void cannotActivateAbility0WhenTapped() {
        Permanent bomb = addReadyBomb(player1);
        bomb.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing with 2 counters destroys MV 2 creatures")
    void destroysManaValue2Permanents() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 2);

        // MV 2 creature on opponent's battlefield
        harness.addToBattlefield(player2, new GoldMyr());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Ratchet Bomb should be in the graveyard (sacrificed as cost)
        harness.assertInGraveyard(player1, "Ratchet Bomb");
        harness.assertNotOnBattlefield(player1, "Ratchet Bomb");

        // Gold Myr (MV 2) should be destroyed
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.assertInGraveyard(player2, "Gold Myr");
    }

    @Test
    @DisplayName("Sacrificing with 0 counters destroys Memnite")
    void destroysManaValue0Permanents() {
        addReadyBomb(player1);
        // 0 charge counters — targets MV 0

        // MV 0 artifact creature
        harness.addToBattlefield(player2, new Memnite());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Memnite (MV 0) should be destroyed
        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    @DisplayName("Does not destroy permanents with different mana value")
    void doesNotDestroyDifferentManaValue() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 2);

        // MV 1 creature (Glint Hawk = {W})
        harness.addToBattlefield(player2, new GlintHawk());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Glint Hawk (MV 1) should survive
        harness.assertOnBattlefield(player2, "Glint Hawk");
    }

    @Test
    @DisplayName("Does not destroy lands even if mana value matches")
    void doesNotDestroyLands() {
        addReadyBomb(player1);
        // 0 charge counters — lands have MV 0 but should be excluded

        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Forest should survive (lands are excluded)
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Destroys permanents on both sides of the battlefield")
    void destroysPermanentsOnBothSides() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 2);

        // MV 2 creatures on both sides
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player2, new GoldMyr());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Both should be destroyed
        harness.assertNotOnBattlefield(player1, "Gold Myr");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
    }

    @Test
    @DisplayName("Destroys enchantments with matching mana value")
    void destroysEnchantments() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 3);

        // The creature survives, so the Aura must be destroyed by the ability itself.
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        Permanent arrestPerm = harness.addToBattlefieldAndReturn(player2, new Arrest());
        arrestPerm.setAttachedTo(myr.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Arrest");
        harness.assertInGraveyard(player2, "Arrest");
        harness.assertOnBattlefield(player2, "Gold Myr");
    }

    @Test
    @DisplayName("Does not destroy indestructible permanents")
    void doesNotDestroyIndestructible() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 1);

        // Darksteel Axe (MV 1, indestructible)
        harness.addToBattlefield(player2, new DarksteelAxe());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Darksteel Axe should survive (indestructible)
        harness.assertOnBattlefield(player2, "Darksteel Axe");
    }

    @Test
    @DisplayName("Ratchet Bomb is sacrificed as a cost (goes to graveyard immediately)")
    void sacrificedAsCost() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        // Should be in graveyard immediately (sacrifice is a cost)
        harness.assertNotOnBattlefield(player1, "Ratchet Bomb");
        harness.assertInGraveyard(player1, "Ratchet Bomb");
    }

    @Test
    @DisplayName("Cannot use the second ability while tapped after the first")
    void cannotUseBothAbilitiesInSameTurn() {
        addReadyBomb(player1);

        // Use ability 0 first (tap to add counter)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Bomb is now tapped, cannot use ability 1
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Charge counter add then sacrifice on separate turns works correctly")
    void addCounterThenSacrificeOnSeparateTurns() {
        Permanent bomb = addReadyBomb(player1);

        // Turn 1: add a counter
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Simulate next turn: untap
        bomb.untap();

        // Add another counter
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        // Simulate next turn: untap
        bomb.untap();

        // Put a MV 2 creature on opponent's side
        harness.addToBattlefield(player2, new GoldMyr());

        // Sacrifice to destroy MV 2 permanents
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.assertInGraveyard(player2, "Gold Myr");
        harness.assertInGraveyard(player1, "Ratchet Bomb");
    }

    @Test
    @DisplayName("A newly entered noncreature Bomb can tap immediately")
    void newlyEnteredBombCanAddCounter() {
        Permanent bomb = harness.addToBattlefieldAndReturn(player1, new RatchetBomb());

        harness.activateAbility(player1, 0, null, null);
        assertThat(bomb.isTapped()).isTrue();
        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();

        assertThat(bomb.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destruction uses the sacrificed Bomb's counters, not another Bomb's")
    void destructionUsesSacrificedBombCounters() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 2);
        Permanent otherBomb = harness.addToBattlefieldAndReturn(player1, new RatchetBomb());
        otherBomb.setCounterCount(CounterType.CHARGE, 1);
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new GlintHawk());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertOnBattlefield(player2, "Gold Myr");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ratchet Bomb");
        harness.assertInGraveyard(player2, "Gold Myr");
        harness.assertOnBattlefield(player2, "Glint Hawk");
    }

    @Test
    @DisplayName("X in a permanent's mana cost is zero regardless of its counters")
    void destroysChimericMassWithZeroCountersOnBomb() {
        addReadyBomb(player1);
        Permanent mass = harness.addToBattlefieldAndReturn(player2, new ChimericMass());
        mass.setCounterCount(CounterType.CHARGE, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chimeric Mass");
        harness.assertInGraveyard(player2, "Chimeric Mass");
    }

    @Test
    @DisplayName("A face-down creature has mana value zero")
    void zeroCountersDestroyFaceDownCreature() {
        addReadyBomb(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Gold Myr");
    }

    @Test
    @DisplayName("A face-down creature does not use its face-up mana value")
    void twoCountersSpareFaceDownCreature() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.CHARGE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    private Permanent addReadyBomb(Player player) {
        return addCreatureReady(player, new RatchetBomb());
    }
}
