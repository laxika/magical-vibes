package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryLarcenistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Library Larcenist draws a card")
    void attackingDrawsCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent larcenist = addCreatureReady(player1, new LibraryLarcenist());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(larcenist)));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
