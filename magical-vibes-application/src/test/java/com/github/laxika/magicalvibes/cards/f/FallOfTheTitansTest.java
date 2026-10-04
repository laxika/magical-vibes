package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallOfTheTitans.class, GrizzlyBears.class, Shock.class})
class FallOfTheTitansTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each of two targets")
    void dealsXDamageToEachTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstantForX(player1, 0, 3, List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Fall of the Titans");
    }

    @Test
    @DisplayName("Casts for its surge cost after another spell was cast")
    void castsForSurgeCost() {
        harness.setHand(player1, List.of(new Shock(), new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fall of the Titans");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast for its surge cost before another spell was cast")
    void surgeCostRequiresAnotherSpell() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Surge pays X once and deals the full X to both targets")
    void surgeWithNonzeroX() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.ensurePriority(player1);
        gs.playCardWithAlternateCost(gd, player1, 0, 3, null, null,
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 15);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Another spell still on the stack enables surge")
    void surgeWhileAnotherSpellIsOnStack() {
        harness.setHand(player1, List.of(new Shock(), new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());

        harness.ensurePriority(player1);
        gs.playCardWithAlternateCost(gd, player1, 0, 2, null, null, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Normal casting pays for both X symbols")
    void normalCostRequiresTwiceX() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 3, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fall of the Titans");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("X may be zero with a chosen target")
    void zeroXWithTarget() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForX(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Fall of the Titans");
    }

    @Test
    @DisplayName("A remaining legal target still receives the full X damage")
    void resolvesWithOneTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallOfTheTitans(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castInstantForX(player1, 0, 3, List.of(creature.getId(), player2.getId()));
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Fall of the Titans");
    }

    @Test
    @DisplayName("Cannot choose the same target twice")
    void rejectsDuplicateTargets() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than two targets")
    void rejectsThreeTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2,
                List.of(creature.getId(), player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's spell does not enable surge")
    void opponentsSpellDoesNotEnableSurge() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can deal the full X to both players, including its controller")
    void canTargetBothPlayers() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstantForX(player1, 0, 2, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
