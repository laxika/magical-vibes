package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Boompile.class, GrizzlyBears.class, Forest.class})
class BoompileTest extends BaseCardTest {

    @Test
    @DisplayName("A winning flip destroys all nonland permanents while lands survive")
    void destroysNonlandsOnlyWhenFlipIsWon() {
        harness.addToBattlefield(player1, new Boompile());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip for Boompile");
        boolean lost = gameLogContains("loses the coin flip for Boompile");
        assertThat(won ^ lost).isTrue();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        if (won) {
            harness.assertNotOnBattlefield(player1, "Boompile");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        } else {
            harness.assertOnBattlefield(player1, "Boompile");
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }
    }
}
