package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneOfTheLiving.class, GrizzlyBears.class, HillGiant.class})
class BaneOfTheLivingTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Bane of the Living face up gives all creatures -X/-X")
    void turnsFaceUpAndWeakensAllCreaturesByPaidX() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bane)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bane of the Living's face-up debuff wears off at end of turn")
    void faceUpDebuffWearsOffAtEndOfTurn() {
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGiant)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGiant)).isEqualTo(3);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new BaneOfTheLiving()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Bane of the Living");
    }
}
