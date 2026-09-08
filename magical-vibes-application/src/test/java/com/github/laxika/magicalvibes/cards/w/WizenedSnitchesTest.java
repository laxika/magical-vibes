package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WizenedSnitches.class, Shock.class, GrizzlyBears.class})
class WizenedSnitchesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals every player's top library card to both players")
    void revealsEveryPlayersTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new WizenedSnitches());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Shock")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Shock")
                        && message.contains("Grizzly Bears"));
    }
}
