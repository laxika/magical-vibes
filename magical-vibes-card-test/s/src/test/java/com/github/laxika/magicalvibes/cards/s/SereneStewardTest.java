package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SereneSteward.class, GrizzlyBears.class})
class SereneStewardTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {W} puts a +1/+1 counter on the chosen creature")
    void payingPutsCounterOnChosenCreature() {
        harness.addToBattlefield(player1, new SereneSteward());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the payment puts no counter")
    void decliningPutsNoCounter() {
        harness.addToBattlefield(player1, new SereneSteward());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The Steward can target itself when it is the only creature")
    void onlyCreatureCanTargetItself() {
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new SereneSteward());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(steward.getId());
    }

    @Test
    @DisplayName("A single gain of several life puts only one counter on the target")
    void largerLifeGainPutsOnlyOneCounter() {
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new SereneSteward());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, steward.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(steward.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent gaining life does not trigger the Steward")
    void opponentLifeGainDoesNotTrigger() {
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new SereneSteward());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(steward.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Separate life gains can each put a counter on the same creature")
    void separateLifeGainsEachTrigger() {
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new SereneSteward());

        for (int i = 0; i < 2; i++) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, steward.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(steward.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonwhite mana cannot pay for the counter")
    void nonwhiteManaCannotPay() {
        Permanent steward = harness.addToBattlefieldAndReturn(player1, new SereneSteward());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, steward.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(steward.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
