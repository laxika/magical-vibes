package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesolationTest extends BaseCardTest {

    @Test
    @DisplayName("Player who tapped a land for mana sacrifices a land at end step")
    void tapsLandSacrificesAtEndStep() {
        harness.addToBattlefield(player1, new Desolation());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        resolveEndStep(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context())
                .isInstanceOf(MultiPermanentChoiceContext.ForcedSacrificeThenDamageIfSubtype.class);

        Permanent forest = findPermanent(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Desolation");
    }

    @Test
    @DisplayName("Sacrificing a Plains deals 2 damage to that player")
    void sacrificingPlainsDealsDamage() {
        harness.addToBattlefield(player1, new Desolation());
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        resolveEndStep(player1);

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sacrificing a non-Plains land deals no damage")
    void sacrificingNonPlainsNoDamage() {
        harness.addToBattlefield(player1, new Desolation());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        resolveEndStep(player1);

        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Player who did not tap a land for mana is unaffected")
    void playerWhoDidNotTapUnaffected() {
        harness.addToBattlefield(player1, new Desolation());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStep(player1);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tracks land taps even if Desolation entered after the tap")
    void worksEvenIfEnteredAfterTap() {
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 0);

        harness.addToBattlefield(player1, new Desolation());

        resolveEndStep(player1);

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Triggers on each end step including an opponent's turn")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new Desolation());
        harness.addToBattlefield(player2, new Plains());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.tapPermanent(player2, 0);

        resolveEndStep(player2);

        harness.assertNotOnBattlefield(player2, "Plains");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Both players who tapped lands choose and sacrifice")
    void bothPlayersWhoTappedSacrifice() {
        harness.addToBattlefield(player1, new Desolation());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player2, 0);

        resolveEndStep(player1);

        PendingInteraction.MultiPermanentChoice p1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(p1Choice).isNotNull();
        assertThat(p1Choice.playerId()).isEqualTo(player1.getId());

        Permanent p1Forest = findPermanent(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(p1Forest.getId()));

        PendingInteraction.MultiPermanentChoice p2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(p2Choice).isNotNull();
        assertThat(p2Choice.playerId()).isEqualTo(player2.getId());

        Permanent p2Plains = findPermanent(player2, "Plains");
        harness.handleMultiplePermanentsChosen(player2, List.of(p2Plains.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 8; i++) {
            harness.passBothPriorities();
            if (gd.currentStep == TurnStep.END_STEP
                    && (gd.interaction.isAwaitingInput() || gd.stack.isEmpty())) {
                break;
            }
        }
        // Resolve the Desolation trigger if still on the stack.
        if (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
    }
}
