package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UbulSarGatekeepersTest extends BaseCardTest {

    @Test
    @DisplayName("With two Gates, the chosen opponent creature gets -2/-2 and dies")
    void twoGatesShrinksOpponentCreature() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trigger prompt only offers creatures an opponent controls")
    void promptOffersOnlyOpponentCreatures() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castGatekeepers();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(opponentBearsId);
    }

    @Test
    @DisplayName("With only one Gate the trigger does not fire and no target is chosen")
    void oneGateDoesNotTrigger() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ubul Sar Gatekeepers");
    }

    @Test
    @DisplayName("Gates an opponent controls do not count")
    void opponentGatesDoNotCount() {
        setUpTurn();
        harness.addToBattlefield(player2, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new BorosGuildgate());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGatekeepers();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castGatekeepers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void castGatekeepers() {
        harness.setHand(player1, List.of(new UbulSarGatekeepers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
