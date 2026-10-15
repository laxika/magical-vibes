package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GobblingOoze.class, DrudgeBeetle.class})
class GobblingOozeTest extends BaseCardTest {

    @Test
    @DisplayName("{G}, Sacrifice another creature: the Ooze gets a +1/+1 counter")
    void sacrificeAnotherCreatureAddsCounter() {
        Permanent ooze = addCreatureReady(player1, new GobblingOoze());
        addCreatureReady(player1, new DrudgeBeetle());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Beetle");
        harness.assertInGraveyard(player1, "Drudge Beetle");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ooze.getEffectivePower()).isEqualTo(4);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot sacrifice the Ooze itself to its own ability")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new GobblingOoze());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations accumulate counters")
    void repeatedActivationsAccumulateCounters() {
        Permanent ooze = addCreatureReady(player1, new GobblingOoze());
        addCreatureReady(player1, new DrudgeBeetle());
        addCreatureReady(player1, new DrudgeBeetle());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Drudge Beetle").getId());
        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without the {G} mana cost")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new GobblingOoze());
        addCreatureReady(player1, new DrudgeBeetle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Drudge Beetle");
    }

    @Test
    @DisplayName("Sacrifice is paid before the counter ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new GobblingOoze());
        harness.addToBattlefield(player1, new DrudgeBeetle());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Drudge Beetle");
        harness.assertNotOnBattlefield(player1, "Drudge Beetle");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ooze can sacrifice a tapped, summoning-sick creature")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new GobblingOoze());
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        ooze.setSummoningSick(true);
        ooze.tap();
        beetle.setSummoningSick(true);
        beetle.tap();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drudge Beetle");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ooze.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new GobblingOoze());
        harness.addToBattlefield(player2, new DrudgeBeetle());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gobbling Ooze");
        harness.assertOnBattlefield(player2, "Drudge Beetle");
    }
}
