package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthServant.class, Mountain.class, Forest.class})
class EarthServantTest extends BaseCardTest {

    @Test
    @DisplayName("Without Mountains, is 4/4")
    void withoutMountainsIs4x4() {
        Permanent earthServant = addEarthServant(player1);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(4);
    }

    @Test
    @DisplayName("With one Mountain, is 4/5")
    void withOneMountainIs4x5() {
        Permanent earthServant = addEarthServant(player1);
        addMountain(player1);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(5);
    }

    @Test
    @DisplayName("With three Mountains, is 4/7")
    void withThreeMountainsIs4x7() {
        Permanent earthServant = addEarthServant(player1);
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(7);
    }

    @Test
    @DisplayName("Opponent's Mountains don't affect Earth Servant's toughness")
    void opponentMountainsDontCount() {
        Permanent earthServant = addEarthServant(player1);
        addMountain(player2);
        addMountain(player2);

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-Mountain lands don't affect Earth Servant's toughness")
    void nonMountainLandsDontCount() {
        Permanent earthServant = addEarthServant(player1);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, earthServant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthServant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Toughness updates as Mountains enter and leave the battlefield")
    void toughnessTracksMountainsEnteringAndLeaving() {
        Permanent servant = addEarthServant(player1);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(4);

        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.setTapped(true);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        harness.setGraveyard(player1, java.util.List.of(mountain.getCard()));
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus uses the current controller's Mountains after control changes")
    void bonusTracksCurrentController() {
        Permanent servant = addEarthServant(player1);
        addMountain(player1);
        addMountain(player2);
        addMountain(player2);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(servant);
        gd.playerBattlefields.get(player2.getId()).add(servant);
        assertThat(gqs.getEffectiveToughness(gd, servant)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, servant)).isEqualTo(4);
    }

    private Permanent addEarthServant(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new EarthServant());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addMountain(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }
}
