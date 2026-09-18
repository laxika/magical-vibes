package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaneOfTheLiving.class, EnormousBaloth.class, FugitiveWizard.class})
class BaneOfTheLivingTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Bane of the Living face up gives all creatures -X/-X")
    void turnsFaceUpAndWeakensAllCreaturesByPaidX() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        Permanent opponentBaloth = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, opponentBaloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentBaloth)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bane)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bane of the Living's face-up debuff wears off at end of turn")
    void faceUpDebuffWearsOffAtEndOfTurn() {
        Permanent opponentBaloth = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        Permanent bane = castFaceDown();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bane));
        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBaloth)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opponentBaloth)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBaloth)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, opponentBaloth)).isEqualTo(7);
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
