package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SevenDwarves.class, GrizzlyBears.class})
class SevenDwarvesTest extends BaseCardTest {

    @Test
    @DisplayName("Seven Dwarves is 2/2 when it is the only one")
    void isBaseStatsAlone() {
        Permanent dwarves = addDwarvesReady(player1);

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven Dwarves gets +1/+1 for each other Seven Dwarves you control")
    void countsOwnOtherDwarves() {
        Permanent dwarves = addDwarvesReady(player1);
        harness.addToBattlefield(player1, new SevenDwarves());
        harness.addToBattlefield(player1, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(4);
    }

    @Test
    @DisplayName("Seven Dwarves does not count opponents' Seven Dwarves")
    void ignoresOpponentDwarves() {
        Permanent dwarves = addDwarvesReady(player1);
        harness.addToBattlefield(player2, new SevenDwarves());
        harness.addToBattlefield(player2, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven Dwarves does not count creatures with different names")
    void ignoresDifferentNames() {
        Permanent dwarves = addDwarvesReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven Dwarves bonus shrinks when another Seven Dwarves leaves the battlefield")
    void bonusUpdatesWhenAnotherDwarfLeaves() {
        Permanent dwarves = addDwarvesReady(player1);
        harness.addToBattlefield(player1, new SevenDwarves());
        harness.addToBattlefield(player1, new SevenDwarves());

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> !p.getId().equals(dwarves.getId()) && p.getCard().getName().equals("Seven Dwarves"));

        assertThat(gqs.getEffectivePower(gd, dwarves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves)).isEqualTo(2);
    }

    private Permanent addDwarvesReady(Player player) {
        Permanent permanent = new Permanent(new SevenDwarves());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
