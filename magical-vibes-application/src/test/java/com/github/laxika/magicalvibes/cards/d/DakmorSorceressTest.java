package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DakmorSorceressTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of Swamps you control; toughness stays 4")
    void powerEqualsControlledSwamps() {
        Permanent sorceress = addSorceressReady(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only your Swamps, not opponent Swamps")
    void countsOnlyControllersSwamps() {
        Permanent sorceress = addSorceressReady(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates as Swamps change; toughness remains 4")
    void powerUpdatesWhenSwampsChange() {
        Permanent sorceress = addSorceressReady(player1);

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);
    }

    private Permanent addSorceressReady(Player player) {
        DakmorSorceress card = new DakmorSorceress();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
