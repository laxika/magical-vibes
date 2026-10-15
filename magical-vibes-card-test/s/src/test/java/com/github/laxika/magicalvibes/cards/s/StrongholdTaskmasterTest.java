package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DungeonShade;
import com.github.laxika.magicalvibes.cards.f.FurnaceSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongholdTaskmaster.class, DungeonShade.class, FurnaceSpirit.class})
class StrongholdTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other black creatures get -1/-1")
    void debuffsOtherBlackCreatures() {
        harness.addToBattlefield(player1, new StrongholdTaskmaster());
        Permanent ownShade = harness.addToBattlefieldAndReturn(player1, new DungeonShade());
        Permanent opponentShade = harness.addToBattlefieldAndReturn(player2, new DungeonShade());

        assertThat(gqs.getEffectivePower(gd, ownShade)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, ownShade)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, opponentShade)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentShade)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not affect itself or nonblack creatures")
    void doesNotAffectItselfOrNonblackCreatures() {
        Permanent taskmaster = harness.addToBattlefieldAndReturn(player1, new StrongholdTaskmaster());
        Permanent furnaceSpirit = harness.addToBattlefieldAndReturn(player1, new FurnaceSpirit());

        assertThat(gqs.getEffectivePower(gd, taskmaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, taskmaster)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, furnaceSpirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, furnaceSpirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Taskmaster affects the other but not itself")
    void eachTaskmasterAffectsTheOtherButNotItself() {
        Permanent firstTaskmaster = harness.addToBattlefieldAndReturn(player1, new StrongholdTaskmaster());
        Permanent secondTaskmaster = harness.addToBattlefieldAndReturn(player1, new StrongholdTaskmaster());

        assertThat(gqs.getEffectivePower(gd, firstTaskmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstTaskmaster)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondTaskmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondTaskmaster)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff is removed when Stronghold Taskmaster leaves")
    void debuffRemovedWhenSourceLeaves() {
        Permanent taskmaster = harness.addToBattlefieldAndReturn(player1, new StrongholdTaskmaster());
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new DungeonShade());

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId()).remove(taskmaster);

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
    }

    @Test
    @DisplayName("Zero-toughness black creatures die on both sides while nonblack creatures survive")
    void zeroToughnessCreaturesDieOnBothSides() {
        harness.addToBattlefield(player1, new DungeonShade());
        harness.addToBattlefield(player2, new DungeonShade());
        harness.addToBattlefield(player2, new FurnaceSpirit());
        harness.enterBattlefieldAndReturn(player1, new StrongholdTaskmaster());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Dungeon Shade");
        harness.assertNotOnBattlefield(player2, "Dungeon Shade");
        harness.assertInGraveyard(player1, "Dungeon Shade");
        harness.assertInGraveyard(player2, "Dungeon Shade");
        harness.assertOnBattlefield(player1, "Stronghold Taskmaster");
        harness.assertOnBattlefield(player2, "Furnace Spirit");
    }

    @Test
    @DisplayName("Taskmasters controlled by different players each apply their debuff")
    void opposingTaskmasterDebuffsAccumulate() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StrongholdTaskmaster());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StrongholdTaskmaster());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new StrongholdTaskmaster());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(1);
    }
}
