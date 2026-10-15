package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChatterfangSquirrelGeneral;
import com.github.laxika.magicalvibes.cards.u.UnholyHeat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquirrelSovereign.class, ChatterfangSquirrelGeneral.class, GrizzlyBears.class, UnholyHeat.class})
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

    @Test
    @DisplayName("Multiple Sovereigns buff each other and their bonuses stack on another Squirrel")
    void multipleSovereignsStackTheirBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SquirrelSovereign());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SquirrelSovereign());
        Permanent squirrel = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(5);
    }

    @Test
    @DisplayName("The bonus ends when Sovereign leaves the battlefield")
    void bonusEndsWhenSovereignLeavesBattlefield() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new SquirrelSovereign());
        Permanent squirrel = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player2, List.of(new UnholyHeat()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(4);

        harness.castAndResolveInstant(player2, 0, sovereign.getId());

        harness.assertNotOnBattlefield(player1, "Squirrel Sovereign");
        harness.assertInGraveyard(player1, "Squirrel Sovereign");
        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(3);
    }
}
