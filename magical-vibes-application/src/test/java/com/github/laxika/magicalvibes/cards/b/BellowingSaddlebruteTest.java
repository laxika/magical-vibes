package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BellowingSaddlebruteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes you lose 4 life when you did not attack this turn")
    void losesLifeWithoutRaid() {
        int lifeBefore = gd.getLife(player1.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("ETB does not make you lose life when raid is met")
    void doesNotLoseLifeWithRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        int lifeBefore = gd.getLife(player1.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("ETB checks raid when its trigger resolves")
    void checksRaidAtResolution() {
        int lifeBefore = gd.getLife(player1.getId());

        castBellowingSaddlebrute();
        harness.passBothPriorities();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    private void castBellowingSaddlebrute() {
        harness.setHand(player1, List.of(new BellowingSaddlebrute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
