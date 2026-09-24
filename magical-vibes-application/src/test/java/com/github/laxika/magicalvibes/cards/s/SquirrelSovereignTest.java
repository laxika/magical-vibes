package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChatterfangSquirrelGeneral;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquirrelSovereign.class, ChatterfangSquirrelGeneral.class, GrizzlyBears.class})
class SquirrelSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Other Squirrels you control get +1/+1")
    void buffsOtherSquirrelsYouControl() {
        harness.addToBattlefield(player1, new SquirrelSovereign());
        Permanent squirrel = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Squirrel Sovereign does not buff itself")
    void doesNotBuffItself() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new SquirrelSovereign());

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(2);
    }

    @Test
    @DisplayName("It does not buff non-Squirrels or Squirrels controlled by an opponent")
    void doesNotBuffNonSquirrelsOrOpponentsSquirrels() {
        harness.addToBattlefield(player1, new SquirrelSovereign());
        Permanent nonSquirrel = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSquirrel = harness.addToBattlefieldAndReturn(player2, new ChatterfangSquirrelGeneral());

        assertThat(gqs.getEffectivePower(gd, nonSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(3);
    }
}
