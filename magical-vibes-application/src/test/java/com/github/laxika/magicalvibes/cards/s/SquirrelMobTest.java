package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquirrelMobTest extends BaseCardTest {

    @Test
    @DisplayName("Squirrel Mob is 2/2 when it is alone")
    void isBaseStatsAlone() {
        Permanent mob = addCreatureReady(player1, new SquirrelMob());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }

    @Test
    @DisplayName("Squirrel Mob gets +1/+1 for each other Squirrel on the battlefield")
    void countsOtherSquirrelsOnAllBattlefields() {
        Permanent mob = addCreatureReady(player1, new SquirrelMob());
        harness.addToBattlefield(player1, new SquirrelMob());
        harness.addToBattlefield(player2, new SquirrelMob());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(4);
    }

    @Test
    @DisplayName("Squirrel Mob ignores non-Squirrels")
    void ignoresNonSquirrels() {
        Permanent mob = addCreatureReady(player1, new SquirrelMob());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }
}
