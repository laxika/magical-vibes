package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TectonicInstabilityTest extends BaseCardTest {

    @Test
    @DisplayName("A land entering under your control taps all your lands but not other permanents")
    void ownLandTapsControlledLands() {
        harness.addToBattlefield(player1, new TectonicInstability());
        Permanent existingLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Island()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(existingLand.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(existingCreature.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's land entering taps all lands that opponent controls")
    void opponentLandTapsOpponentsLands() {
        harness.addToBattlefield(player1, new TectonicInstability());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(opponentLand.isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
    }
}
