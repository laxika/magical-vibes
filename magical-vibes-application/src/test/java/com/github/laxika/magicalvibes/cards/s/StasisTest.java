package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StasisTest extends BaseCardTest {

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
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

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    @Test
    @DisplayName("Declining to pay {U} sacrifices Stasis")
    void decliningPaymentSacrificesStasis() {
        addReady(player1, new Stasis());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Stasis"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Stasis"));
    }

    @Test
    @DisplayName("Paying {U} keeps Stasis on the battlefield")
    void payingKeepsStasis() {
        addReady(player1, new Stasis());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stasis"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        addReady(player1, new Stasis());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stasis"));
    }
}
