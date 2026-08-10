package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NimShriekerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact controlled by its controller")
    void getsPowerForControlledArtifacts() {
        harness.addToBattlefield(player1, new NimShrieker());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent nim = findPermanent(player1, "Nim Shrieker");

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(1);
    }
}
