package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarduHateblade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodChinFanatic.class, MarduHateblade.class, GrizzlyBears.class})
class BloodChinFanaticTest extends BaseCardTest {

    @Test
    void sacrificesAnotherWarriorAndDrainsTargetPlayer() {
        addReadyFanatic();
        harness.addToBattlefield(player1, new MarduHateblade());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Mardu Hateblade");
    }

    @Test
    void usesSacrificedWarriorsEffectivePower() {
        addReadyFanatic();
        var warrior = harness.addToBattlefieldAndReturn(player1, new MarduHateblade());
        warrior.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void cannotSacrificeNonWarriorOrTheSource() {
        addReadyFanatic();
        harness.addToBattlefield(player1, new GrizzlyBears());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCreatureAsTargetPlayer() {
        addReadyFanatic();
        harness.addToBattlefield(player1, new MarduHateblade());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItsController() {
        addReadyFanatic();
        harness.addToBattlefield(player1, new BloodChinFanatic());
        harness.setLife(player1, 10);
        addMana();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.assertInGraveyard(player1, "Blood-Chin Fanatic");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Blood-Chin Fanatic");
    }

    @Test
    void cannotSacrificeAnOpponentsWarrior() {
        addReadyFanatic();
        harness.addToBattlefield(player2, new BloodChinFanatic());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Blood-Chin Fanatic");
        harness.assertOnBattlefield(player2, "Blood-Chin Fanatic");
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        addReadyFanatic();
        harness.addToBattlefield(player1, new BloodChinFanatic());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void usesChosenWarriorsPowerWhenMultipleSacrificesAreAvailable() {
        addReadyFanatic();
        var chosen = harness.addToBattlefieldAndReturn(player1, new BloodChinFanatic());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        var other = harness.addToBattlefieldAndReturn(player1, new BloodChinFanatic());
        harness.setLife(player1, 10);
        addMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.assertInGraveyard(player1, "Blood-Chin Fanatic");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other).doesNotContain(chosen);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    private void addReadyFanatic() {
        harness.addToBattlefield(player1, new BloodChinFanatic());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
