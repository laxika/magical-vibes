package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiritualGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, controller gains 4 life")
    void gainsFourLifeOnEnter() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new SpiritualGuardian()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature onto battlefield
        harness.passBothPriorities(); // resolve ETB gain-life trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }
}
