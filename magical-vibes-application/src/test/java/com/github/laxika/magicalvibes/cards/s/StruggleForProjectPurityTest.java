package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StruggleForProjectPurity.class, GrizzlyBears.class})
class StruggleForProjectPurityTest extends BaseCardTest {

    @Test
    @DisplayName("Brotherhood makes each opponent draw, then the controller draws for each card drawn")
    void brotherhoodDrawsForEachOpponentCardDrawn() {
        Card opponentCard = new GrizzlyBears();
        Card controllerCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setLibrary(player2, List.of(opponentCard));
        castCard("Brotherhood");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(controllerCard);
    }

    @Test
    @DisplayName("Enclave gives the attacking player twice the number of creatures attacking you in rad counters")
    void enclaveGivesTwiceTheNumberOfAttackersInRadCounters() {
        castCard("Enclave");
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    private void castCard(String mode) {
        harness.setHand(player1, List.of(new StruggleForProjectPurity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
