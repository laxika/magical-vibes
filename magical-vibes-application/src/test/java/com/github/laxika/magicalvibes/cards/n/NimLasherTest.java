package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimLasher.class, Ornithopter.class})
class NimLasherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact controlled by its controller")
    void getsPowerForControlledArtifacts() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimLasher());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus updates as a controlled artifact enters and leaves")
    void bonusTracksControlledArtifactsDynamically() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimLasher());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(1);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().tryDestroyPermanent(gd, artifact));

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(1);
    }
}
