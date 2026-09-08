package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MilitantInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Militant Inquisitor gets +1/+0 for each Equipment you control")
    void getsPowerForEachControlledEquipment() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent-controlled Equipment does not boost Militant Inquisitor")
    void opponentEquipmentDoesNotCount() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);
    }
}
