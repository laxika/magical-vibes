package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.u.UtromMonitor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplodingBarrel.class, UtromMonitor.class})
class ExplodingBarrelTest extends BaseCardTest {

    @Test
    @DisplayName("Adds mana of the chosen color and a pressure counter")
    void addsManaAndPressureCounter() {
        Permanent barrel = addReadyBarrel();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(barrel.getCounterCount(CounterType.PRESSURE)).isEqualTo(1);
        assertThat(barrel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 20 damage and sacrifices itself")
    void dealsDamageAndSacrificesItself() {
        Permanent barrel = addReadyBarrel();
        Permanent target = addCreatureReady(player2, new UtromMonitor());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(barrel);
        harness.assertInGraveyard(player1, "Exploding Barrel");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Utrom Monitor");
        harness.assertInGraveyard(player2, "Utrom Monitor");
    }

    @Test
    @DisplayName("Costs one less for each pressure counter")
    void costsLessForPressureCounters() {
        addReadyBarrel().setCounterCount(CounterType.PRESSURE, 3);
        Permanent target = addCreatureReady(player2, new UtromMonitor());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Utrom Monitor");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent barrel = addReadyBarrel();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, barrel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void manaAbilityResolvesImmediatelyAndAccumulatesPressure(ManaColor color) {
        Permanent barrel = harness.addToBattlefieldAndReturn(player1, new ExplodingBarrel());
        barrel.setCounterCount(CounterType.PRESSURE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(barrel.getCounterCount(CounterType.PRESSURE)).isEqualTo(3);
        assertThat(barrel.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12})
    void pressureCanReduceCostToZero(int counters) {
        addReadyBarrel().setCounterCount(CounterType.PRESSURE, counters);
        Permanent target = addCreatureReady(player1, new UtromMonitor());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Exploding Barrel");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Utrom Monitor");
    }

    @Test
    void otherCountersDoNotReduceCost() {
        Permanent barrel = addReadyBarrel();
        barrel.setCounterCount(CounterType.CHARGE, 8);
        Permanent target = addCreatureReady(player2, new UtromMonitor());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(barrel.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Exploding Barrel");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(7);
    }

    @Test
    void cannotExplodeOutsideMainPhase() {
        Permanent barrel = addReadyBarrel();
        Permanent target = addCreatureReady(player2, new UtromMonitor());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(barrel.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Exploding Barrel");
    }

    @Test
    void cannotExplodeDuringOpponentsMainPhase() {
        addReadyBarrel();
        Permanent target = addCreatureReady(player2, new UtromMonitor());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Exploding Barrel");
    }

    @Test
    void dealsExactlyTwentyDamage() {
        addReadyBarrel();
        Permanent target = addCreatureReady(player2, new UtromMonitor());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Utrom Monitor");
        assertThat(target.getMarkedDamage()).isEqualTo(20);
    }

    @Test
    void cannotExplodeWhileAnotherAbilityIsOnStack() {
        addReadyBarrel().setCounterCount(CounterType.PRESSURE, 8);
        Permanent secondBarrel = addReadyBarrel();
        secondBarrel.setCounterCount(CounterType.PRESSURE, 8);
        Permanent target = addCreatureReady(player2, new UtromMonitor());

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(secondBarrel.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Utrom Monitor");
    }

    @Test
    void manaAbilityCanBeUsedDuringOpponentsUpkeep() {
        Permanent barrel = addReadyBarrel();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.stack).isEmpty();
        assertThat(barrel.getCounterCount(CounterType.PRESSURE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void tappingForManaPreventsExplosionUntilUntapped() {
        Permanent barrel = addReadyBarrel();
        Permanent target = addCreatureReady(player2, new UtromMonitor());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        barrel.setTapped(false);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Utrom Monitor");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    private Permanent addReadyBarrel() {
        Permanent barrel = harness.addToBattlefieldAndReturn(player1, new ExplodingBarrel());
        barrel.setSummoningSick(false);
        return barrel;
    }
}
