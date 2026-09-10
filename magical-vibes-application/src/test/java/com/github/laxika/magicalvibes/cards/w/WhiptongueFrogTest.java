package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WhiptongueFrog.class)
class WhiptongueFrogTest extends BaseCardTest {

    @Test
    @DisplayName("{U}: Whiptongue Frog gains flying until end of turn")
    void gainsFlyingUntilEndOfTurn() {
        Permanent frog = harness.addToBattlefieldAndReturn(player1, new WhiptongueFrog());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, frog, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent frog = harness.addToBattlefieldAndReturn(player1, new WhiptongueFrog());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, frog, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The ability requires blue mana")
    void requiresBlueMana() {
        Permanent frog = harness.addToBattlefieldAndReturn(player1, new WhiptongueFrog());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.hasKeyword(gd, frog, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The ability grants flying only to its source")
    void grantsFlyingOnlyToItsSource() {
        Permanent frog = harness.addToBattlefieldAndReturn(player1, new WhiptongueFrog());
        Permanent otherFrog = harness.addToBattlefieldAndReturn(player1, new WhiptongueFrog());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, frog, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherFrog, Keyword.FLYING)).isFalse();
    }
}
