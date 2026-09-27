package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DomineeringWill.class, GrizzlyBears.class})
class DomineeringWillTest extends BaseCardTest {

    @Test
    void gainsControlUntapsAndForcesAllChosenCreaturesToBlock() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.tap();
        second.tap();

        castDomineeringWill(first, second);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(first.getId(), second.getId());
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(second.isMustBlockThisTurnIfAble()).isTrue();
    }

    @Test
    void controlAndBlockRequirementExpireAtEndOfTurn() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        castDomineeringWill(first, second);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(first.getId(), second.getId());
        assertThat(first.isMustBlockThisTurnIfAble()).isFalse();
        assertThat(second.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    void cannotTargetAnAttackingCreature() {
        Permanent attacking = addCreatureReady(player1, new GrizzlyBears());
        attacking.setAttacking(true);
        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(player2.getId(), attacking.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonattacking creature");
    }

    private void castDomineeringWill(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(player2.getId(), first.getId(), second.getId()));
        harness.passBothPriorities();
    }
}
