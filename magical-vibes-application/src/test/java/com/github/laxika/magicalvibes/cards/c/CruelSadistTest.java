package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Ulcerate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelSadist.class, RuneclawBear.class, Ulcerate.class})
class CruelSadistTest extends BaseCardTest {

    @Test
    @DisplayName("First ability pays life, taps, and puts a +1/+1 counter on this creature")
    void firstAbility() {
        Permanent sadist = addReadySadist(player1, 0);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(sadist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability removes the chosen number of counters and deals that much damage")
    void secondAbilityUsesChosenCounterAmount() {
        Permanent sadist = addReadySadist(player1, 3);
        Permanent bears = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void lifeAndTapArePaidBeforeTheCounterIsPlaced() {
        Permanent sadist = addReadySadist(player1, 0);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(sadist.isTapped()).isTrue();
        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void countersAreRemovedBeforeDamageIsDealt() {
        Permanent sadist = addReadySadist(player1, 3);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, 1, bear.getId());

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(sadist.isTapped()).isTrue();
        assertThat(bear.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void zeroCountersCanBeChosenAndDealNoDamage() {
        Permanent sadist = addReadySadist(player1, 0);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.handleXValueChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(sadist.isTapped()).isTrue();
        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void cannotRemoveMoreCountersThanArePresent() {
        Permanent sadist = addReadySadist(player1, 1);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sadist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothAbilitiesRequireTheCreatureToBeReadyToTap() {
        Permanent sadist = harness.addToBattlefieldAndReturn(player1, new CruelSadist());
        sadist.setSummoningSick(true);
        sadist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sadist.isTapped()).isFalse();
        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageStillResolvesAfterTheSourceDies() {
        Permanent sadist = addReadySadist(player1, 2);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Ulcerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 1, 2, bear.getId());
        harness.castInstant(player1, 0, sadist.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cruel Sadist");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    void growthAbilityDoesNotReturnTheSourceAfterItDies() {
        Permanent sadist = addReadySadist(player1, 0);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Ulcerate()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player1, 0, sadist.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Cruel Sadist");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cruel Sadist");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageAbilityCannotTargetAPlayer() {
        Permanent sadist = addReadySadist(player1, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sadist.isTapped()).isFalse();
        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSourceCannotActivateEitherAbility() {
        Permanent sadist = addReadySadist(player1, 2);
        sadist.setTapped(true);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItsOwnControllerCreature() {
        Permanent sadist = addReadySadist(player1, 2);
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 1, 2, bear.getId());
        harness.passBothPriorities();

        assertThat(sadist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    private Permanent addReadySadist(Player player, int counters) {
        Permanent sadist = addCreatureReady(player, new CruelSadist());
        sadist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return sadist;
    }
}
