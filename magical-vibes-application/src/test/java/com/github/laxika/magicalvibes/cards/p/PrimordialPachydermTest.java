package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PrimordialPachyderm.class)
class PrimordialPachydermTest extends BaseCardTest {

    @Test
    @DisplayName("When Primordial Pachyderm enters, its controller gains 2 life")
    void gainsLifeWhenItEnters() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrimordialPachyderm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
