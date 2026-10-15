package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalecasterColossus.class, GracefulAdept.class, GrizzlyBears.class, Island.class})
class GalecasterColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target nonland permanent an opponent controls to its owner's hand")
    void returnsTargetNonlandPermanent() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new GalecasterColossus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(colossus.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can tap another Wizard to pay the activation cost")
    void canTapAnotherWizardAsCost() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new GalecasterColossus());
        colossus.tap();
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new GracefulAdept());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.passBothPriorities();

        assertThat(wizard.isTapped()).isTrue();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GalecasterColossus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the ability's controller")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GalecasterColossus());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}
