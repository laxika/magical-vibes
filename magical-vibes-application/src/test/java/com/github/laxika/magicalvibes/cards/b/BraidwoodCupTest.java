package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BraidwoodCupTest extends BaseCardTest {

    @Test
    void tappingBraidwoodCupGainsOneLife() {
        Permanent cup = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(cup.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new BraidwoodCup());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
