package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpawningPitTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature puts a charge counter on Spawning Pit")
    void sacrificingCreatureAddsChargeCounter() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(pit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing two charge counters creates a 2/2 colorless Spawn artifact creature token")
    void removesTwoChargeCountersAndCreatesSpawn() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(pit.getCounterCount(CounterType.CHARGE)).isZero();
        Permanent spawn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(spawn.getCard().getSubtypes()).contains(CardSubtype.SPAWN);
        assertThat(spawn.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(spawn.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spawn.getEffectivePower()).isEqualTo(2);
        assertThat(spawn.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot create a Spawn without two charge counters")
    void cannotCreateSpawnWithOneChargeCounter() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
