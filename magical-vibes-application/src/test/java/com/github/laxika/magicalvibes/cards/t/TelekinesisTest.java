package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Telekinesis.class, GrizzlyBears.class, Forest.class})
class TelekinesisTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target, prevents its combat damage, and skips its next two untap steps")
    void appliesAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(2);

        target.untap();
        target.setSummoningSick(false);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        harness.setLife(player1, 20);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Keeps the target tapped through its controller's next two untap steps")
    void skipsNextTwoUntapSteps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
