package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeathbloomThallid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FungalBloomTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a spore counter on a target Fungus")
    void putsSporeCounterOnTargetFungus() {
        harness.addToBattlefield(player1, new FungalBloom());
        Permanent fungus = harness.addToBattlefieldAndReturn(player2, new DeathbloomThallid());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, fungus.getId());
        harness.passBothPriorities();

        assertThat(fungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can't target a non-Fungus creature")
    void cannotTargetNonFungus() {
        harness.addToBattlefield(player1, new FungalBloom());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
