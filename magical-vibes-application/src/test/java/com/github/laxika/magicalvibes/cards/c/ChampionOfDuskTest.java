package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws and loses life equal to the number of Vampires you control")
    void etbDrawsAndLosesLifeForControlledVampires() {
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new VampireInterloper());
        prepareDeck(2);
        castChampion();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB counts Champion of Dusk itself as a Vampire")
    void etbCountsItself() {
        prepareDeck(1);
        castChampion();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private void castChampion() {
        harness.setHand(player1, List.of(new ChampionOfDusk()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
    }

    private void prepareDeck(int forestCount) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < forestCount; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }
}
