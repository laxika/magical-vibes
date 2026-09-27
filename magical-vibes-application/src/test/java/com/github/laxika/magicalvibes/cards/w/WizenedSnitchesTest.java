package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.z.ZephyrSpirit;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WizenedSnitches.class, Island.class, ZephyrSpirit.class})
class WizenedSnitchesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals every player's top library card to both players")
    void revealsEveryPlayersTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new WizenedSnitches());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new ZephyrSpirit()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Island")
                        && message.contains("Zephyr Spirit"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Island")
                        && message.contains("Zephyr Spirit"));
    }

    @Test
    @DisplayName("Stops revealing library tops when Wizened Snitches leaves the battlefield")
    void stopsRevealingWhenItLeavesBattlefield() {
        harness.addToBattlefield(player1, new WizenedSnitches());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new ZephyrSpirit()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }
}
