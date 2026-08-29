package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdelbertSteiner.class, LeoninScimitar.class})
class AdelbertSteinerTest extends BaseCardTest {

    @Test
    @DisplayName("Adelbert Steiner gets +1/+1 for each Equipment you control")
    void getsBoostForEachControlledEquipment() {
        Permanent steiner = harness.addToBattlefieldAndReturn(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent-controlled Equipment does not boost Adelbert Steiner")
    void opponentEquipmentDoesNotCount() {
        Permanent steiner = harness.addToBattlefieldAndReturn(player1, new AdelbertSteiner());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(1);
    }
}
