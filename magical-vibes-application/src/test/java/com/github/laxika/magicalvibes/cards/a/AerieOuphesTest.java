package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AerieOuphesTest extends BaseCardTest {

    // Sacrificing Aerie Ouphes also puts its Persist trigger on the stack above the ability,
    // so resolve the whole stack (persist return first, then the damage ability via LKI power).
    private void resolveStack() {
        for (int i = 0; i < 12 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Deals damage equal to its power (3), killing a 3/3 flyer")
    void dealsPowerDamageKillingFlyer() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AngelOfMercy());

        Permanent target = findPermanent(player2, "Angel of Mercy");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        resolveStack();

        harness.assertNotOnBattlefield(player2, "Angel of Mercy");
        harness.assertInGraveyard(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("A 4/4 flyer survives the 3 damage")
    void higherToughnessFlyerSurvives() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AirElemental());

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        resolveStack();

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Aerie Ouphes is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AngelOfMercy());

        Permanent target = findPermanent(player2, "Angel of Mercy");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Aerie Ouphes");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }
}
