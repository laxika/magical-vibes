package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stasis.class, GrizzlyBears.class, Forest.class, Breezekeeper.class})
class StasisTest extends BaseCardTest {

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (untap step)
    }

    @Test
    @DisplayName("Controller's tapped permanents stay tapped through their untap step")
    void controllerPermanentsStayTapped() {
        addCreatureReady(player1, new Stasis());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2); // player1's untap step

        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's tapped permanents stay tapped through their untap step")
    void opponentPermanentsStayTapped() {
        addCreatureReady(player1, new Stasis());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        oppBears.tap();

        advanceToNextTurn(player1); // player2's untap step

        assertThat(oppBears.isTapped()).isTrue();
    }

    @Test
    void skipPreventsPhasing() {
        addCreatureReady(player1, new Stasis());
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());

        advanceToNextTurn(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(keeper);
    }

    @Test
    @DisplayName("Once Stasis leaves, permanents untap again")
    void untapsAfterStasisLeaves() {
        Permanent stasis = addCreatureReady(player1, new Stasis());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        gd.playerBattlefields.get(player1.getId()).remove(stasis);

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay {U} sacrifices Stasis")
    void decliningPaymentSacrificesStasis() {
        addCreatureReady(player1, new Stasis());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Stasis");
        harness.assertInGraveyard(player1, "Stasis");
    }

    @Test
    @DisplayName("Paying {U} keeps Stasis on the battlefield")
    void payingKeepsStasis() {
        addCreatureReady(player1, new Stasis());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Stasis");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        addCreatureReady(player1, new Stasis());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stasis");
    }

    @Test
    void cannotPayUpkeepWithGreenMana() {
        harness.addToBattlefield(player1, new Stasis());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Stasis");
        harness.assertInGraveyard(player1, "Stasis");
    }

    @Test
    void mayDeclineUpkeepEvenWithBlueManaAvailable() {
        harness.addToBattlefield(player1, new Stasis());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Stasis");
        harness.assertInGraveyard(player1, "Stasis");
    }

    @Test
    void sacrificingDuringUpkeepDoesNotRetroactivelyUntapPermanents() {
        harness.addToBattlefield(player1, new Stasis());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Stasis");
        assertThat(bears.isTapped()).isTrue();
    }
}
