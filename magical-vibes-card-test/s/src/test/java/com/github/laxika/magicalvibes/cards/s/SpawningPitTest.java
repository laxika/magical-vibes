package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawningPit.class, CrazedGoblin.class})
class SpawningPitTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature puts a charge counter on Spawning Pit")
    void sacrificingCreatureAddsChargeCounter() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        harness.addToBattlefield(player1, new CrazedGoblin());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(pit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Crazed Goblin");
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
        Permanent spawn = findPermanent(player1, "Spawn");
        assertThat(spawn.getCard().isToken()).isTrue();
        assertThat(spawn.getCard().getSubtypes()).contains(CardSubtype.SPAWN);
        assertThat(spawn.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(spawn.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spawn.getEffectivePower()).isEqualTo(2);
        assertThat(spawn.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Spawn token is colorless")
    void createsColorlessSpawn() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spawn").getCard().getColor()).isNull();
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

    @Test
    @DisplayName("Cannot create a Spawn without paying one generic mana")
    void cannotCreateSpawnWithoutMana() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pit.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Spawn");
    }

    @Test
    @DisplayName("The creature is sacrificed as a cost before the charge counter is added")
    void sacrificeIsPaidBeforeResolution() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        harness.addToBattlefield(player1, new CrazedGoblin());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Crazed Goblin");
        harness.assertNotOnBattlefield(player1, "Crazed Goblin");
        assertThat(pit.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        assertThat(pit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        harness.addToBattlefield(player2, new CrazedGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Crazed Goblin");
        assertThat(pit.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("A tapped Pit removes exactly two counters as a cost before creating a Spawn")
    void tappedPitPaysCountersBeforeResolution() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.tap();
        pit.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(pit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Spawn");

        harness.passBothPriorities();

        assertThat(pit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Spawn");
        assertThat(findPermanent(player1, "Spawn").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A created Spawn can be sacrificed back to Spawning Pit")
    void canSacrificeCreatedSpawn() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spawn");
        assertThat(pit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }
}
