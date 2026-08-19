package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RishkarPeemaRenegadeTest extends BaseCardTest {

    @Test
    void putsCountersOnUpToTwoTargetCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RishkarPeemaRenegade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canEnterWithoutTargets() {
        harness.setHand(player1, List.of(new RishkarPeemaRenegade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rishkar, Peema Renegade");
    }

    @Test
    void onlyCounterBearingCreaturesCanTapForGreenMana() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent rishkar = addCreatureReady(player1, new RishkarPeemaRenegade());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rishkar.isTapped()).isFalse();
    }

    @Test
    void rishkarWithACounterCanTapForGreenMana() {
        Permanent rishkar = addCreatureReady(player1, new RishkarPeemaRenegade());
        rishkar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(rishkar.isTapped()).isTrue();
    }
}
