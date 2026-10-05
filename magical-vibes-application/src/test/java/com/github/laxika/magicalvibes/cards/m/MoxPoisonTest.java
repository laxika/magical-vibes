package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void givesManaAndPoisonToThePlayerActivatingIt() {
        harness.addToBattlefield(player2, new MoxPoison());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new MoxPoison());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void reachingTenPoisonCountersLosesTheGameAfterTheManaAbility() {
        harness.addToBattlefield(player1, new MoxPoison());
        gd.playerPoisonCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
