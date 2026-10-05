package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalevolentNoble.class, GoldenEgg.class, Gingerbrute.class})
class MalevolentNobleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on the Noble")
    void sacrificingAnotherCreatureAddsCounter() {
        Permanent noble = addNobleReady();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MalevolentNoble());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Malevolent Noble");
        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing an artifact puts a +1/+1 counter on the Noble")
    void sacrificingArtifactAddsCounter() {
        Permanent noble = addNobleReady();
        harness.addToBattlefield(player1, new GoldenEgg());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Golden Egg");
        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Noble cannot sacrifice itself")
    void cannotSacrificeItself() {
        addNobleReady();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Sacrifice is paid before the counter is placed on resolution")
    void sacrificeIsAnImmediateCost() {
        Permanent noble = harness.addToBattlefieldAndReturn(player1, new MalevolentNoble());
        harness.addToBattlefield(player1, new Gingerbrute());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.assertNotOnBattlefield(player1, "Gingerbrute");
        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick and tapped Noble can activate its ability")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent noble = harness.addToBattlefieldAndReturn(player1, new MalevolentNoble());
        noble.setSummoningSick(true);
        noble.setTapped(true);
        harness.addToBattlefield(player1, new GoldenEgg());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Golden Egg");
        assertThat(noble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opposing artifacts and creatures cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsPermanents() {
        addNobleReady();
        harness.addToBattlefield(player2, new GoldenEgg());
        harness.addToBattlefield(player2, new MalevolentNoble());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");

        harness.assertOnBattlefield(player2, "Golden Egg");
        harness.assertOnBattlefield(player2, "Malevolent Noble");
    }

    private Permanent addNobleReady() {
        Permanent noble = harness.addToBattlefieldAndReturn(player1, new MalevolentNoble());
        noble.setSummoningSick(false);
        return noble;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
