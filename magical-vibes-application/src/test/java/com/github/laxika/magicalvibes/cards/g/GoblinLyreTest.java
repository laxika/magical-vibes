package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.Pyroblast;
import com.github.laxika.magicalvibes.cards.w.WordOfUndoing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinLyre.class, BalduvianBears.class, JaceBeleren.class, Pyroblast.class, WordOfUndoing.class})
class GoblinLyreTest extends BaseCardTest {

    @Test
    @DisplayName("Winning damages the opponent, losing damages you, by the respective creature counts")
    void flipDamagesOneSideByCreatureCount() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Sacrifice is a cost, so the Lyre is gone either way.
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");

        boolean won = gameLogContains("wins the coin flip for Goblin Lyre");
        boolean lost = gameLogContains("loses the coin flip for Goblin Lyre");
        assertThat(won ^ lost).isTrue();

        if (won) {
            // Three creatures the controller controls.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
        } else {
            // One creature the targeted opponent controls.
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore - 1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        }
    }

    @Test
    @DisplayName("Neither player takes damage when the relevant battlefield has no creatures")
    void noCreaturesMeansNoDamage() {
        harness.addToBattlefield(player1, new GoblinLyre());

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Goblin Lyre")).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("A planeswalker target uses its controller's creature count on a lost flip")
    void targetsPlaneswalkerAndUsesItsControllerForLoss() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Goblin Lyre");
        boolean lost = gameLogContains("loses the coin flip for Goblin Lyre");
        assertThat(won ^ lost).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");

        if (won) {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 2);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        } else {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore - 1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        }
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("A planeswalker controlled by the ability's controller is a legal target")
    void canTargetOwnPlaneswalker() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new BalduvianBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Goblin Lyre");
        boolean lost = gameLogContains("loses the coin flip for Goblin Lyre");
        assertThat(won ^ lost).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Lyre");
        harness.assertInGraveyard(player1, "Goblin Lyre");

        if (won) {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore);
        } else {
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLifeBefore - 1);
        }
    }

    @Test
    @DisplayName("The controller cannot be chosen as the target")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new GoblinLyre());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .hasMessageContaining("opponent");
        harness.assertOnBattlefield(player1, "Goblin Lyre");
        harness.assertNotInGraveyard(player1, "Goblin Lyre");
    }

    @Test
    @DisplayName("A creature cannot be chosen as the target")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new GoblinLyre());
        Permanent creature = addCreatureReady(player2, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Goblin Lyre");
        harness.assertNotInGraveyard(player1, "Goblin Lyre");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature counts are determined on resolution rather than activation")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new GoblinLyre());
        Permanent ownCreature = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());
        Permanent opposingCreature = addCreatureReady(player2, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new WordOfUndoing(), new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Goblin Lyre");
        assertThat(gameLogContains("coin flip for Goblin Lyre")).isFalse();
        harness.castAndResolveInstant(player1, 0, ownCreature.getId());
        harness.castAndResolveInstant(player1, 0, opposingCreature.getId());
        harness.assertInHand(player1, "Balduvian Bears");
        harness.assertInHand(player2, "Balduvian Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Goblin Lyre");
        boolean lost = gameLogContains("loses the coin flip for Goblin Lyre");
        assertThat(won ^ lost).isTrue();
        harness.assertLife(player1, ownLifeBefore - (lost ? 1 : 0));
        harness.assertLife(player2, opponentLifeBefore - (won ? 2 : 0));
    }

    @Test
    @CardUsed({JaceBeleren.class, Pyroblast.class})
    @DisplayName("An ability whose planeswalker target leaves does not flip a coin or deal damage")
    void removedPlaneswalkerTargetStopsEntireAbility() {
        harness.addToBattlefield(player1, new GoblinLyre());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player1, List.of(new Pyroblast()));
        harness.addMana(player1, ManaColor.RED, 1);
        int ownLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.castInstant(player1, 0, 1, planeswalker.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("coin flip for Goblin Lyre")).isFalse();
        harness.assertInGraveyard(player1, "Goblin Lyre");
        harness.assertLife(player1, ownLifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }
}
