package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservoirWalkerTest extends BaseCardTest {

    @Test
    void entersWithLifeGainAndEnergyCounters() {
        harness.setHand(player1, List.of(new ReservoirWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }
}
