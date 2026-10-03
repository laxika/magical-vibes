package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondersEnclave.class, ColossalDreadmaw.class, GrizzlyBears.class})
class BondersEnclaveTest extends BaseCardTest {

    @Test
    void addsColorlessMana() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(enclave.isTapped()).isTrue();
    }

    @Test
    void drawsCardWhenControllingCreatureWithPowerFourOrGreater() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(enclave), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(enclave.isTapped()).isTrue();
    }

    @Test
    void cannotDrawWithoutControllingCreatureWithPowerFourOrGreater() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enclave), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a creature with power 4 or greater");
    }

    @Test
    void drawsWithExactlyFourPowerFromCountersEvenWhenCreatureIsTapped() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.tap();
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, indexOf(enclave), 1, null, null);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(enclave.isTapped()).isTrue();
    }

    @Test
    void cannotDrawWithOnlyThreePower() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enclave), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a creature with power 4 or greater");
        assertThat(enclave.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void opponentsCreatureDoesNotEnableDrawing() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enclave), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a creature with power 4 or greater");
        assertThat(enclave.isTapped()).isFalse();
    }

    @Test
    void stillDrawsWhenCreaturePowerFallsBelowFourBeforeResolution() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(enclave), 1, null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void cannotDrawWithInsufficientMana() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enclave), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enclave.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void cannotDrawAfterTappingForMana() {
        Permanent enclave = harness.addToBattlefieldAndReturn(player1, new BondersEnclave());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(enclave), 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enclave), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
