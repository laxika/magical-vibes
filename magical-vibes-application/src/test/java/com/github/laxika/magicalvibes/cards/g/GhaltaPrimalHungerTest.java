package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhaltaPrimalHunger.class, BearCub.class})
class GhaltaPrimalHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {10}{G}{G} with no power among creatures you control")
    void costsFullAmountWithoutControlledCreatures() {
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 11);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs less by the total power of creatures you control")
    void reducesCostByControlledCreaturePower() {
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player1, new BearCub());
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent creatures do not reduce the cost")
    void ignoresOpponentsCreatures() {
        harness.addToBattlefield(player2, new BearCub());
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 11);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Negative power subtracts from the total power")
    void negativePowerSubtractsFromTotal() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player1, new BearCub());
        weakened.setPowerModifier(-3);
        harness.addToBattlefield(player1, new BearCub());
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 11);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    void negativeTotalPowerDoesNotIncreaseCost() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player1, new BearCub());
        weakened.setPowerModifier(-3);
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 12);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessPowerReducesCostToTwoGreenMana() {
        harness.addToBattlefield(player1, new GhaltaPrimalHunger());
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessPowerCannotReduceColoredManaRequirements() {
        harness.addToBattlefield(player1, new GhaltaPrimalHunger());
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void countsModifiedPowerOfTappedCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearCub());
        creature.setPowerModifier(3);
        creature.setTapped(true);
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void tramplesOverBlockerAfterAssigningLethalDamage() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GhaltaPrimalHunger());
        Permanent blocker = addCreatureReady(player2, new BearCub());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 11)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 10));

        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player2, "Bear Cub");
        harness.assertOnBattlefield(player1, "Ghalta, Primal Hunger");
    }
}
