package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShamanOfThePackTest extends BaseCardTest {

    private void castShaman() {
        harness.setHand(player1, new ArrayList<>(List.of(new ShamanOfThePack())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target opponent loses life equal to the Elves you control, counting the Shaman itself")
    void drainsForEachElfIncludingItself() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        castShaman();
        harness.passBothPriorities(); // resolve the ETB trigger

        // Two Llanowar Elves + the Shaman itself; the Bears and the opponent's Elf don't count.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With no other Elves the opponent still loses 1 life for the Shaman itself")
    void drainsOneWithNoOtherElves() {
        castShaman();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
