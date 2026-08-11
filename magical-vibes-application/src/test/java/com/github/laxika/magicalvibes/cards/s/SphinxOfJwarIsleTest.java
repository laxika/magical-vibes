package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SphinxOfJwarIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Only its controller can see the top card of their library")
    void onlyControllerSeesOwnLibraryTopCard() {
        harness.addToBattlefield(player1, new SphinxOfJwarIsle());
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.clearMessages();

        harness.passPriority(player1);

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Air Elemental"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Air Elemental"));
    }
}
