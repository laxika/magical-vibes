package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.c.ChildOfThorns;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoratamiMindsweeper.class, TendoIceBridge.class, GodsEyeGateToTheReikai.class,
        GnarledMass.class, ChildOfThorns.class, BileUrchin.class})
class SoratamiMindsweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as cost, then target player mills two cards")
    void returnsLandThenMills() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.setLibrary(player2, List.of(new GnarledMass(), new ChildOfThorns(), new BileUrchin()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player2.getId());

        harness.assertInHand(player1, "Tendo Ice Bridge");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tendo Ice Bridge"));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Child of Thorns");
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a nonland as the activation cost")
    void cannotReturnNonlandAsCost() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addToBattlefield(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chooses which land to return when several are available")
    void choosesLandWhenSeveralAvailable() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player1, new GodsEyeGateToTheReikai());
        harness.setLibrary(player2, List.of(new GnarledMass(), new ChildOfThorns()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent godsEye = findPermanent(player1, "Gods' Eye, Gate to the Reikai");

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player2.getId());

        assertThat(gd.stack).isEmpty();

        harness.handlePermanentChosen(player1, godsEye.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Gods' Eye, Gate to the Reikai");
        harness.assertOnBattlefield(player1, "Tendo Ice Bridge");
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new SoratamiMindsweeper());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.setLibrary(player1, List.of(new GnarledMass(), new ChildOfThorns(), new BileUrchin()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mindsweeper"), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Gnarled Mass");
        harness.assertInGraveyard(player1, "Child of Thorns");
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
