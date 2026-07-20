package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HazoretTheFerventTest extends BaseCardTest {

    // ===== {2}{R}, Discard a card: Hazoret deals 2 damage to each opponent =====

    @Test
    @DisplayName("Discarding a card deals 2 damage to each opponent")
    void abilityDealsTwoDamageToEachOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate the ability with no card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new HazoretTheFervent());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Can't attack or block unless you have one or fewer cards in hand =====

    @Test
    @DisplayName("Cannot attack with two or more cards in hand")
    void cannotAttackWithTwoCardsInHand() {
        addCreatureReady(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island(), new Island()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack with one or fewer cards in hand")
    void canAttackWithOneCardInHand() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot block with two or more cards in hand")
    void cannotBlockWithTwoCardsInHand() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new HazoretTheFervent());
        harness.setHand(player1, List.of(new Island(), new Island()));

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
