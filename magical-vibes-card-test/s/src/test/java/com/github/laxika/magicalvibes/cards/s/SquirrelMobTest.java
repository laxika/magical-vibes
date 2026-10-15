package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquirrelMob.class, DuskImp.class})
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
        addCreatureReady(player1, new SquirrelMob());
        addCreatureReady(player2, new SquirrelMob());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(4);
    }

    @Test
    @DisplayName("Squirrel Mob ignores non-Squirrels")
    void ignoresNonSquirrels() {
        Permanent mob = addCreatureReady(player1, new SquirrelMob());
        addCreatureReady(player1, new DuskImp());
        addCreatureReady(player2, new DuskImp());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }

    @Test
    @DisplayName("Squirrel Mob updates its boost as another Squirrel enters and leaves")
    void updatesWhenAnotherSquirrelEntersAndLeaves() {
        Permanent mob = addCreatureReady(player1, new SquirrelMob());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);

        Permanent other = harness.enterBattlefieldAndReturn(player2, new SquirrelMob());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, other));

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }

    @Test
    @DisplayName("Squirrel Mob does not count Squirrels outside the battlefield")
    void ignoresSquirrelsInOtherZones() {
        Permanent mob = addCreatureReady(player1, new SquirrelMob());
        harness.setHand(player1, List.of(new SquirrelMob()));
        harness.setLibrary(player2, List.of(new SquirrelMob()));
        harness.setGraveyard(player1, List.of(new SquirrelMob()));
        harness.setExile(player2, List.of(new SquirrelMob()));

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }
}
