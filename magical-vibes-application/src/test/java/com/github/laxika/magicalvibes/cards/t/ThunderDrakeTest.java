package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderDrake.class, LightningBolt.class})
class ThunderDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when you cast your second spell each turn")
    void putsCounterOnSecondSpell() {
        Permanent drake = addCreatureReady(player1, new ThunderDrake());
        int initialPower = drake.getEffectivePower();
        int initialToughness = drake.getEffectiveToughness();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(drake.getEffectivePower()).isEqualTo(initialPower);
        assertThat(drake.getEffectiveToughness()).isEqualTo(initialToughness);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(drake.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(drake.getEffectiveToughness()).isEqualTo(initialToughness + 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(drake.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(drake.getEffectiveToughness()).isEqualTo(initialToughness + 1);
    }
}
