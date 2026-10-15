package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornMammoth.class, GrizzlyBears.class})
class ThornMammothTest extends BaseCardTest {

    @Test
    @DisplayName("Fights an opponent creature when it enters")
    void fightsWhenItEnters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new ThornMammoth());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fights an opponent creature when another creature you control enters")
    void fightsWhenAnotherCreatureEnters() {
        Permanent mammoth = harness.addToBattlefieldAndReturn(player1, new ThornMammoth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(mammoth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can choose no target for the fight")
    void canChooseNoTarget() {
        Permanent mammoth = harness.addToBattlefieldAndReturn(player1, new ThornMammoth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(mammoth.getMarkedDamage()).isZero();
    }
}
