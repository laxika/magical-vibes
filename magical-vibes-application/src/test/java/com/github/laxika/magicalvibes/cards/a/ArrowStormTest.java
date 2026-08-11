package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArrowStormTest extends BaseCardTest {

    @Test
    void dealsFourDamageWithoutRaid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void dealsFiveDamageWithRaid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void normalDamageCanBePrevented() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GlacialChasm());
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void raidDamageCannotBePrevented() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GlacialChasm());
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
