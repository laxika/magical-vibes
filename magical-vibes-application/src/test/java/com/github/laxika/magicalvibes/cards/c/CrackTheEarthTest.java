package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrackTheEarth.class, GnarledMass.class, GodsEyeGateToTheReikai.class, TendoIceBridge.class})
class CrackTheEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Each player with exactly one permanent loses it automatically")
    void eachPlayerWithOnePermanentLosesIt() {
        harness.addToBattlefield(player1, new GnarledMass());
        harness.addToBattlefield(player2, new TendoIceBridge());

        harness.castFromHand(player1, new CrackTheEarth(), "{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gnarled Mass");
        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
    }

    @Test
    @DisplayName("A player with several permanents chooses which one to sacrifice")
    void playerWithSeveralPermanentsChooses() {
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.addToBattlefield(player2, new GodsEyeGateToTheReikai());

        harness.castFromHand(player1, new CrackTheEarth(), "{R}");

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent tendo = findPermanent(player2, "Tendo Ice Bridge");
        harness.handleMultiplePermanentsChosen(player2, List.of(tendo.getId()));

        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player2, "Gods' Eye, Gate to the Reikai");
    }

    @Test
    @DisplayName("Each player chooses one permanent when both control several")
    void eachPlayerChoosesOnePermanentWhenBothControlSeveral() {
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player1, new GodsEyeGateToTheReikai());
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.addToBattlefield(player2, new GodsEyeGateToTheReikai());

        harness.castFromHand(player1, new CrackTheEarth(), "{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        Permanent player1Tendo = findPermanent(player1, "Tendo Ice Bridge");
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Tendo.getId()));

        choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        Permanent player2Tendo = findPermanent(player2, "Tendo Ice Bridge");
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Tendo.getId()));

        harness.assertNotOnBattlefield(player1, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player1, "Gods' Eye, Gate to the Reikai");
        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player2, "Gods' Eye, Gate to the Reikai");
    }

    @Test
    @DisplayName("Any permanent type can be sacrificed, including lands")
    void anyPermanentTypeCanBeSacrificed() {
        harness.addToBattlefield(player2, new TendoIceBridge());

        harness.castFromHand(player1, new CrackTheEarth(), "{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
    }

    @Test
    @DisplayName("A player with no permanents has nothing to sacrifice")
    void playerWithNoPermanentsHasNothingToSacrifice() {
        harness.addToBattlefield(player1, new GnarledMass());

        harness.castFromHand(player1, new CrackTheEarth(), "{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gnarled Mass");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
