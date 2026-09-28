package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoxPoison.class)
class MoxPoisonTest extends BaseCardTest {

    @Test
    void tapsForAnyColorAndGivesItsControllerTwoPoisonCounters() {
        for (ManaColor color : List.of(
                ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN)) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            gd = harness.getGameData();
            harness.skipMulligan();

            Permanent mox = harness.addToBattlefieldAndReturn(player1, new MoxPoison());

            harness.activateAbility(player1, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
            assertThat(mox.isTapped()).isTrue();
            assertThat(gd.stack).isEmpty();
        }
    }
}
