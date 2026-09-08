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

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

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

    // ===== Players skip their untap steps =====

    @Test
    @DisplayName("Controller's tapped permanents stay tapped through their untap step")
    void controllerPermanentsStayTapped() {
        addReady(player1, new Stasis());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2); // player1's untap step

        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's tapped permanents stay tapped through their untap step")
    void opponentPermanentsStayTapped() {
        addReady(player1, new Stasis());
        Permanent oppBears = addReady(player2, new GrizzlyBears());
        oppBears.tap();

        advanceToNextTurn(player1); // player2's untap step

        assertThat(oppBears.isTapped()).isTrue();
    }

    @Test
    void skipPreventsPhasing() {
        addReady(player1, new Stasis());
        Permanent keeper = addReady(player1, new Breezekeeper());

        advanceToNextTurn(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(keeper);
    }

    @Test
    @DisplayName("Once Stasis leaves, permanents untap again")
    void untapsAfterStasisLeaves() {
        Permanent stasis = addReady(player1, new Stasis());
        Permanent bears = addReady(player1, new GrizzlyBears());
        bears.tap();

        gd.playerBattlefields.get(player1.getId()).remove(stasis);

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    // ===== Upkeep sacrifice-unless-pay {U} =====

    @Test
    @DisplayName("Declining to pay {U} sacrifices Stasis")
    void decliningPaymentSacrificesStasis() {
        addReady(player1, new Stasis());

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
        addReady(player1, new Stasis());

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
        addReady(player1, new Stasis());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stasis");
    }
}
