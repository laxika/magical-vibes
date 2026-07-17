package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrimordialOozeTest extends BaseCardTest {

    private void advanceToUpkeepAndResolveTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires onto the stack
        harness.passBothPriorities(); // resolve trigger → counter placed, then may-pay prompt
    }

    @Test
    @DisplayName("Upkeep adds a +1/+1 counter, and declining to pay taps it and deals X damage to you")
    void declineTapsAndDealsCounterDamage() {
        harness.addToBattlefield(player1, new PrimordialOoze());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeepAndResolveTrigger(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent ooze = findPermanent(player1, "Primordial Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ooze.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Paying {X} keeps it untapped, deals no damage, and spends the mana")
    void payAvoidsPenalty() {
        harness.addToBattlefield(player1, new PrimordialOoze());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeepAndResolveTrigger(player1);
        harness.addMana(player1, ManaColor.RED, 1); // X = 1 counter
        harness.handleMayAbilityChosen(player1, true);

        Permanent ooze = findPermanent(player1, "Primordial Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ooze.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still taps it and deals the damage")
    void acceptWithoutManaAppliesPenalty() {
        harness.addToBattlefield(player1, new PrimordialOoze());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeepAndResolveTrigger(player1);
        harness.handleMayAbilityChosen(player1, true); // no mana in pool

        Permanent ooze = findPermanent(player1, "Primordial Ooze");
        assertThat(ooze.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PrimordialOoze());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeepAndResolveTrigger(player2);

        Permanent ooze = findPermanent(player1, "Primordial Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ooze.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
