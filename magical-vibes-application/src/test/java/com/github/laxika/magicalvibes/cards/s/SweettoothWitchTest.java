package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SweettoothWitch.class)
class SweettoothWitchTest extends BaseCardTest {

    @Test
    void entersAndCreatesFoodToken() {
        harness.setHand(player1, List.of(new SweettoothWitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void sacrificesFoodToMakeTargetPlayerLoseTwoLife() {
        harness.setHand(player1, List.of(new SweettoothWitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
