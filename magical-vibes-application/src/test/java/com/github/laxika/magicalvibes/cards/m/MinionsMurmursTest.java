package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinionsMurmurs.class, BlindPhantasm.class, HorizonCanopy.class})
class MinionsMurmursTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and loses life equal to the number of creatures you control")
    void drawsAndLosesLifeForControlledCreatures() {
        harness.addToBattlefield(player1, new BlindPhantasm());
        harness.addToBattlefield(player1, new BlindPhantasm());
        harness.addToBattlefield(player2, new BlindPhantasm());
        harness.setLibrary(player1, List.of(new BlindPhantasm(), new BlindPhantasm(), new BlindPhantasm()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new MinionsMurmurs(), "{2}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not count noncreatures or creatures controlled by an opponent")
    void countsOnlyCreaturesYouControl() {
        harness.addToBattlefield(player1, new BlindPhantasm());
        harness.addToBattlefield(player1, new HorizonCanopy());
        harness.addToBattlefield(player2, new BlindPhantasm());
        harness.setLibrary(player1, List.of(new BlindPhantasm(), new BlindPhantasm(), new BlindPhantasm()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new MinionsMurmurs(), "{2}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does nothing when you control no creatures")
    void doesNothingWithoutCreatures() {
        harness.setLibrary(player1, List.of(new BlindPhantasm()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new MinionsMurmurs(), "{2}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts creatures at resolution rather than when cast")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new BlindPhantasm());
        harness.setLibrary(player1, List.of(new BlindPhantasm(), new BlindPhantasm(), new BlindPhantasm()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new MinionsMurmurs(), "{2}{B}{B}");

        harness.addToBattlefield(player1, new BlindPhantasm());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
