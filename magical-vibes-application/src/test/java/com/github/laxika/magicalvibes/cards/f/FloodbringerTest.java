package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Floodbringer.class, GodsEyeGateToTheReikai.class, GnarledMass.class})
class FloodbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost and taps the targeted land")
    void returnsLandAndTapsTarget() {
        harness.addToBattlefield(player1, new Floodbringer());
        harness.addToBattlefield(player1, new GodsEyeGateToTheReikai());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());

        harness.activateAbility(player1, 0, null, targetLand.getId());

        harness.assertInHand(player1, "Gods' Eye, Gate to the Reikai");
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new Floodbringer());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Floodbringer());
        harness.addToBattlefield(player1, new GodsEyeGateToTheReikai());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Chooses which land to return when several are available")
    void choosesWhichLandToReturn() {
        harness.addToBattlefield(player1, new Floodbringer());
        Permanent landToKeep = harness.addToBattlefieldAndReturn(player1, new GodsEyeGateToTheReikai());
        Permanent landToReturn = harness.addToBattlefieldAndReturn(player1, new GodsEyeGateToTheReikai());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, targetLand.getId());

        assertThat(gd.stack).isEmpty();
        harness.handlePermanentChosen(player1, landToReturn.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(landToKeep)
                .doesNotContain(landToReturn);
        assertThat(gd.playerHands.get(player1.getId())).contains(landToReturn.getCard());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }
}
