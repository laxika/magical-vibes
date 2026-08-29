package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SurrakarBanisherTest extends BaseCardTest {

    private Permanent tappedBears(Player controller) {
        Permanent bears = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        bears.tap();
        return bears;
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SurrakarBanisher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a tapped opponent creature to its owner's hand")
    void etbBouncesTappedOpponentCreature() {
        UUID bearsId = tappedBears(player2).getId();
        castAndResolve();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }

    @Test
    @DisplayName("ETB can return a tapped creature you control")
    void etbBouncesTappedOwnCreature() {
        UUID bearsId = tappedBears(player1).getId();
        castAndResolve();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }

    @Test
    @DisplayName("Declining the may leaves the tapped creature on the battlefield")
    void decliningMayLeavesCreature() {
        UUID bearsId = tappedBears(player2).getId();
        castAndResolve();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }

    @Test
    @DisplayName("An untapped creature is not a legal target")
    void untappedCreatureIsNotTargetable() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SurrakarBanisher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Surrakar Banisher");
    }
}
