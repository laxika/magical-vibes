package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeditsDragoons.class})
class JeditsDragoonsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and its controller gains 4 life")
    void entersAndControllerGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);
        harness.setHand(player1, List.of(new JeditsDragoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }
}
