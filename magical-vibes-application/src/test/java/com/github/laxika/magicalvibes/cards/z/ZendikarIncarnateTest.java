package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZendikarIncarnateTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of lands you control; toughness stays 4")
    void powerEqualsControlledLands() {
        Permanent incarnate = addIncarnate(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, incarnate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incarnate)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only your lands, not opponent lands")
    void countsOnlyControllersLands() {
        Permanent incarnate = addIncarnate(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, incarnate)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, incarnate)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates as lands come and go; toughness remains 4")
    void powerUpdatesWhenLandsChange() {
        Permanent incarnate = addIncarnate(player1);

        assertThat(gqs.getEffectivePower(gd, incarnate)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, incarnate)).isEqualTo(4);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, incarnate)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectivePower(gd, incarnate)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, incarnate)).isEqualTo(4);
    }

    private Permanent addIncarnate(Player player) {
        Permanent permanent = new Permanent(new ZendikarIncarnate());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
