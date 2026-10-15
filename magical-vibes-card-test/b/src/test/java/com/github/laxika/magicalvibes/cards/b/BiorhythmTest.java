package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Biorhythm.class, GrizzlyBears.class, Forest.class})
class BiorhythmTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's life total becomes the number of creatures they control")
    void setsLifeToCreatureCount() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 2);
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("A player controlling no creatures has their life total set to 0")
    void setsLifeToZeroWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 0);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void ignoresNoncreaturePermanents() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 0);
    }
    @Test
    @DisplayName("A player gains life when their creature count exceeds their life total")
    void increasesLifeToCreatureCount() {
        harness.setLife(player1, 1);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 2);
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("Both players having no creatures ends the game in a draw")
    void noCreaturesForEitherPlayerDrawsGame() {
        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
