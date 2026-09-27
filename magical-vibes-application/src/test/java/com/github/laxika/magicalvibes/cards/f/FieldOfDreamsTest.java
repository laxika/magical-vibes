package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieldOfDreams.class, FlashCounter.class, ForceSpike.class})
class FieldOfDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals every player's top library card to both players")
    void revealsEveryPlayersTopLibraryCardToBothPlayers() {
        harness.addToBattlefield(player1, new FieldOfDreams());
        harness.setLibrary(player1, List.of(new FlashCounter()));
        harness.setLibrary(player2, List.of(new ForceSpike()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter")
                        && message.contains("Force Spike"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Flash Counter")
                        && message.contains("Force Spike"));
    }
}
