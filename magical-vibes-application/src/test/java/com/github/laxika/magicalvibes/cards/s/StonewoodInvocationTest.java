package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonewoodInvocation.class, GrizzlyBears.class, Shock.class, Island.class})
class StonewoodInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +5/+5 and shroud until end of turn")
    void boostsAndGrantsShroudUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castStonewoodInvocation(target);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents the target from being targeted by a spell")
    void shroudPreventsTargeting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StonewoodInvocation(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new StonewoodInvocation()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castStonewoodInvocation(Permanent target) {
        harness.setHand(player1, List.of(new StonewoodInvocation()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
