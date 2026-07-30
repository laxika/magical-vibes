package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KessigMalcontentsTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new KessigMalcontents()));
        harness.addMana(player1, ManaColor.RED, 3); // {2}{R}
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the ETB trigger
    }

    @Test
    @DisplayName("Deals damage equal to the number of Humans you control, counting itself")
    void countsItself() {
        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Counts other Humans you control")
    void countsOtherHumans() {
        harness.addToBattlefield(player1, new DoomedTraveler());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Ignores opponents' Humans and your non-Humans")
    void ignoresOpponentHumansAndNonHumans() {
        harness.addToBattlefield(player2, new DoomedTraveler());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }
}
