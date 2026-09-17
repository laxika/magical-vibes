package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplodingBarrel.class, GrizzlyBears.class})
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
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(barrel);
        harness.assertInGraveyard(player1, "Exploding Barrel");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Costs one less for each pressure counter")
    void costsLessForPressureCounters() {
        addReadyBarrel().setCounterCount(CounterType.PRESSURE, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
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

    private Permanent addReadyBarrel() {
        Permanent barrel = harness.addToBattlefieldAndReturn(player1, new ExplodingBarrel());
        barrel.setSummoningSick(false);
        return barrel;
    }
}
