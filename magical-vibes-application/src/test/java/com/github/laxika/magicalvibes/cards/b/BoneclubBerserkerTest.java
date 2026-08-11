package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoneclubBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Boneclub Berserker has base power and toughness without other Goblins")
    void hasBaseStatsAlone() {
        Permanent berserker = addBerserkerReady(player1);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boneclub Berserker gets +2/+0 for each other Goblin you control")
    void countsOtherGoblinsYouControl() {
        Permanent berserker = addBerserkerReady(player1);
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new BoggartBrute());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boneclub Berserker does not count opposing or non-Goblin creatures")
    void ignoresOpposingAndNonGoblinCreatures() {
        Permanent berserker = addBerserkerReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BoggartBrute());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boneclub Berserker updates when another Goblin leaves the battlefield")
    void updatesWhenOtherGoblinLeaves() {
        Permanent berserker = addBerserkerReady(player1);
        harness.addToBattlefield(player1, new BoggartBrute());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Boggart Brute"));

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(2);
    }

    private Permanent addBerserkerReady(Player player) {
        Permanent permanent = new Permanent(new BoneclubBerserker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
