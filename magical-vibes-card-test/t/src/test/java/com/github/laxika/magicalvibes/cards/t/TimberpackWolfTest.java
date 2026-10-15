package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimberpackWolf.class, ArborElf.class})
class TimberpackWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Timberpack Wolf is 2/2 when it is the only one")
    void isBaseStatsAlone() {
        Permanent wolf = addWolfReady(player1);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Timberpack Wolf gets +1/+1 for each other Timberpack Wolf you control")
    void countsOwnOtherWolves() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player1, new TimberpackWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Timberpack Wolf does not count opponents' Timberpack Wolves")
    void ignoresOpponentWolves() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player2, new TimberpackWolf());
        harness.addToBattlefield(player2, new TimberpackWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Timberpack Wolf does not count creatures with different names")
    void ignoresDifferentNames() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player1, new ArborElf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Timberpack Wolf bonus shrinks when another Timberpack Wolf leaves the battlefield")
    void bonusUpdatesWhenOtherWolfLeaves() {
        Permanent wolf = addWolfReady(player1);
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player1, new TimberpackWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> !p.getId().equals(wolf.getId()) && p.getCard().getName().equals("Timberpack Wolf"));

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Timberpack Wolf gets the bonus immediately, even while tapped or summoning sick")
    void eachWolfGetsBonusImmediately() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        first.tap();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Timberpack Wolves outside the battlefield do not contribute to the bonus")
    void ignoresWolvesInOtherZones() {
        Permanent wolf = addWolfReady(player1);
        harness.setHand(player1, List.of(new TimberpackWolf()));
        harness.setLibrary(player1, List.of(new TimberpackWolf()));
        harness.setGraveyard(player1, List.of(new TimberpackWolf()));
        harness.setExile(player1, List.of(new TimberpackWolf()));

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    private Permanent addWolfReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TimberpackWolf());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
