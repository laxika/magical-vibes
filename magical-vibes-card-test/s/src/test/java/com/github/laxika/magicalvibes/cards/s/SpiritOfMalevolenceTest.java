package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritOfMalevolence.class, Shock.class})
class SpiritOfMalevolenceTest extends BaseCardTest {

    @Test
    @DisplayName("When Spirit of Malevolence dies, each opponent loses 1 life and you gain 1 life")
    void deathDrainsOpponentAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfMalevolence());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, spirit.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
