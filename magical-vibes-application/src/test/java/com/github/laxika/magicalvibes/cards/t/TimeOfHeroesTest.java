package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaravanEscort;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimeOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts only creatures you control with level counters")
    void boostsOnlyControlledCreaturesWithLevelCounters() {
        harness.addToBattlefield(player1, new TimeOfHeroes());
        Permanent ownLeveled = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent ownUnleveled = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingLeveled = harness.addToBattlefieldAndReturn(player2, new CaravanEscort());

        levelUp(player1, 1);
        levelUp(player2, 0);

        assertThat(gqs.getEffectivePower(gd, ownLeveled)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownLeveled)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownUnleveled)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownUnleveled)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingLeveled)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingLeveled)).isEqualTo(2);
    }

    private void levelUp(Player player, int permanentIndex) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.activateAbility(player, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }
}
