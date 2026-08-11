package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlindingSprayTest extends BaseCardTest {

    @Test
    @DisplayName("Gives opponents' creatures -4/-0 and draws a card")
    void weakensOpponentsCreaturesAndDraws() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingSpray()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(-2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingSpray()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(opponent.getEffectivePower()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponent.getEffectivePower()).isEqualTo(2);
    }
}
