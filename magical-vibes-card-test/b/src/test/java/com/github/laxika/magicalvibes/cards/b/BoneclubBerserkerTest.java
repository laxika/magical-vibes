package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneclubBerserker.class, BoggartBrute.class, GrizzlyBears.class})
class BoneclubBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Boneclub Berserker has base power and toughness without other Goblins")
    void hasBaseStatsAlone() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new BoneclubBerserker());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boneclub Berserker gets +2/+0 for each other Goblin you control")
    void countsOtherGoblinsYouControl() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new BoneclubBerserker());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new BoggartBrute());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boneclub Berserker does not count opposing or non-Goblin creatures")
    void ignoresOpposingAndNonGoblinCreatures() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new BoneclubBerserker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BoggartBrute());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boneclub Berserker updates when another Goblin leaves the battlefield")
    void updatesWhenOtherGoblinLeaves() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new BoneclubBerserker());
        harness.addToBattlefield(player1, new BoggartBrute());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Boggart Brute"));

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Boneclub Berserkers count one another but not themselves")
    void countsAnotherBerserker() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BoneclubBerserker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BoneclubBerserker());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }
}
