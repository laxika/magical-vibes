package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormfistCrusader.class, GrizzlyBears.class})
class StormfistCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, each player draws a card and loses 1 life")
    void eachPlayerDrawsAndLosesLifeDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new StormfistCrusader());
        GrizzlyBears player1Draw = new GrizzlyBears();
        GrizzlyBears player2Draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Draw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new StormfistCrusader());
        GrizzlyBears player1Draw = new GrizzlyBears();
        GrizzlyBears player2Draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(player2Draw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
