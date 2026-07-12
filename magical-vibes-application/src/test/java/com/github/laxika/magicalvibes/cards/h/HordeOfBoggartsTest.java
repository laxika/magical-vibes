package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HordeOfBoggartsTest extends BaseCardTest {

    @Test
    @DisplayName("Horde of Boggarts is 1/1 when it is your only red permanent (counts itself)")
    void isOneOneWhenOnlyRedPermanent() {
        Permanent horde = addHordeReady(player1);

        assertThat(gqs.getEffectivePower(gd, horde)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, horde)).isEqualTo(1);
    }

    @Test
    @DisplayName("Horde of Boggarts power and toughness equal red permanents you control")
    void ptEqualsControlledRedPermanents() {
        Permanent horde = addHordeReady(player1);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, horde)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, horde)).isEqualTo(3);
    }

    @Test
    @DisplayName("Horde of Boggarts ignores non-red and opponent permanents")
    void ignoresNonRedAndOpponentPermanents() {
        Permanent horde = addHordeReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, horde)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, horde)).isEqualTo(1);
    }

    private Permanent addHordeReady(Player player) {
        Permanent permanent = new Permanent(new HordeOfBoggarts());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
