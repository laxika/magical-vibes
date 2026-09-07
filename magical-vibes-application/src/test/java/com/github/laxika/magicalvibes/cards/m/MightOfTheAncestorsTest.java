package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightOfTheAncestors.class, GrizzlyBears.class})
class MightOfTheAncestorsTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat gives a controlled creature +2/+0 and vigilance")
    void boostsAndGrantsVigilanceToTarget() {
        addMight();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The trigger can target only a creature you control")
    void targetsOnlyCreaturesYouControl() {
        addMight();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and vigilance expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        addMight();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveBeginningOfCombat(player1, target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addMight() {
        return harness.addToBattlefieldAndReturn(player1, new MightOfTheAncestors());
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
