package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmeltWardGatekeepersTest extends BaseCardTest {

    @Test
    @DisplayName("With two Gates, the chosen opponent creature is stolen, untapped and gains haste")
    void twoGatesStealsOpponentCreature() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(bears.getId())).isTrue();
    }

    @Test
    @DisplayName("Trigger prompt only offers creatures an opponent controls")
    void promptOffersOnlyOpponentCreatures() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        castGatekeepers();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(opponentBears.getId());
    }

    @Test
    @DisplayName("With only one Gate the trigger does not fire and no creature is stolen")
    void oneGateDoesNotTrigger() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGatekeepers();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
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
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castGatekeepers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
    }

    private void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void castGatekeepers() {
        harness.setHand(player1, List.of(new SmeltWardGatekeepers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
