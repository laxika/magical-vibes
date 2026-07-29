package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SavageTwisterTest extends BaseCardTest {

    private void castTwister(int xValue) {
        harness.setHand(player1, List.of(new SavageTwister()));
        harness.addMana(player1, ManaColor.RED, 1 + xValue);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Savage Twister deals X damage to each creature, killing those with toughness <= X")
    void dealsXDamageToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castTwister(3);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Savage Twister damages fliers too")
    void damagesFliers() {
        harness.addToBattlefield(player2, new AirElemental());

        castTwister(4);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Creatures with toughness greater than X survive")
    void toughCreaturesSurvive() {
        harness.addToBattlefield(player2, new HillGiant());

        castTwister(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Savage Twister deals no damage to players")
    void dealsNoDamageToPlayers() {
        castTwister(3);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Savage Twister with X=0 kills nothing")
    void xZeroKillsNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castTwister(0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
