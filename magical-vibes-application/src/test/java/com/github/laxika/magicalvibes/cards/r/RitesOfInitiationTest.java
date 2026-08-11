package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RitesOfInitiationTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures +1/+0 for each randomly discarded card")
    void boostsOwnCreaturesByRandomDiscardCount() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Choosing zero cards leaves creatures unchanged")
    void canDiscardZeroCards() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
