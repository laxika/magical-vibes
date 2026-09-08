package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueBoiler.class, Forest.class, GrizzlyBears.class})
class PlagueBoilerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a plague counter at upkeep and destroys nonland permanents at three counters")
    void upkeepCounterTriggersBoardWipeAtThreeCounters() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        boiler.setCounterCount(CounterType.PLAGUE, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(3);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plague Boiler");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The activated ability adds or removes a plague counter")
    void activatedAbilityAddsOrRemovesCounter() {
        Permanent boiler = harness.addToBattlefieldAndReturn(player1, new PlagueBoiler());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a plague counter on Plague Boiler");
        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);

        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Remove a plague counter from Plague Boiler");
        assertThat(boiler.getCounterCount(CounterType.PLAGUE)).isZero();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
