package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PorcelainZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat gives a non-toxic target +1/+1")
    void boostsNonToxicTarget() {
        addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Beginning of combat gives a toxic target +2/+2 instead")
    void boostsToxicTargetByTwoInsteadOfOne() {
        addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger targets only creatures you control")
    void targetsOnlyCreaturesYouControl() {
        Permanent zealot = addZealot();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(zealot.getId(), ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The beginning-of-combat boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveBeginningOfCombat(player1, target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private Permanent addZealot() {
        return harness.addToBattlefieldAndReturn(player1, new PorcelainZealot());
    }

    private void resolveBeginningOfCombat(Player activePlayer, Permanent target) {
        advanceToBeginningOfCombat(activePlayer);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
