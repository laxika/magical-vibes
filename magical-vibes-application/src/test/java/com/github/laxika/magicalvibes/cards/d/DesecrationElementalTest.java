package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesecrationElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Controller sacrifices a creature when they cast a spell")
    void controllerSacrificesCreatureOnOwnSpellCast() {
        harness.addToBattlefield(player1, new DesecrationElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Desecration Elemental");
    }

    @Test
    @DisplayName("An opponent casting a spell still makes the Elemental's controller sacrifice")
    void opponentSpellMakesControllerSacrifice() {
        harness.addToBattlefield(player1, new DesecrationElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("The trigger sacrifices the Elemental if it is the only creature")
    void sourceCanBeSacrificed() {
        harness.addToBattlefield(player1, new DesecrationElemental());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Desecration Elemental");
    }
}
