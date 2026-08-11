package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MalakirBloodwitchTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses life equal to your Vampires and you gain that much")
    void etbDrainsForVampiresYouControl() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new VampireNighthawk());
        harness.setHand(player1, List.of(new MalakirBloodwitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }
}
