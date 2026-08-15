package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquelchingLeechesTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals the number of Swamps you control")
    void ptEqualsControlledSwampCount() {
        Permanent leeches = addLeeches(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(3);
    }

    @Test
    @DisplayName("Is 0/0 with no Swamps")
    void zeroWithoutSwamps() {
        Permanent leeches = addLeeches(player1);

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only your Swamps")
    void countsOnlyControllersSwamps() {
        Permanent leeches = addLeeches(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when a Swamp leaves the battlefield")
    void ptUpdatesWhenSwampsChange() {
        Permanent leeches = addLeeches(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Swamp);

        assertThat(gqs.getEffectivePower(gd, leeches)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, leeches)).isEqualTo(0);
    }

    private Permanent addLeeches(Player player) {
        Permanent permanent = new Permanent(new SquelchingLeeches());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
