package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DiseasedVermin.class)
class DiseasedVerminTest extends BaseCardTest {

    @Test
    @DisplayName("Gets an infection counter when it deals combat damage to a player")
    void getsInfectionCounterOnCombatDamage() {
        Permanent vermin = addReadyVerminThatDealsCombatDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(vermin.getCounterCount(CounterType.INFECTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("At upkeep, deals damage equal to its infection counters to the opponent")
    void upkeepDealsDamageEqualToInfectionCounters() {
        Permanent vermin = addReadyVerminThatDealsCombatDamage();
        vermin.setCounterCount(CounterType.INFECTION, 2);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId()); // choose the target opponent
        harness.passBothPriorities(); // resolve the upkeep trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The upkeep trigger cannot target its controller")
    void upkeepTriggerCannotTargetController() {
        addReadyVerminThatDealsCombatDamage();

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice.class).validPlayerIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("With no infection counters, the upkeep trigger deals no damage")
    void upkeepWithNoCountersDealsNoDamage() {
        Permanent vermin = addReadyVerminThatDealsCombatDamage();
        vermin.setCounterCount(CounterType.INFECTION, 0);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for an opponent it has not previously damaged")
    void upkeepDoesNotTriggerForPreviouslyUndamagedOpponent() {
        Permanent vermin = addReadyVermin();
        vermin.setCounterCount(CounterType.INFECTION, 2);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyVerminThatDealsCombatDamage() {
        Permanent vermin = addReadyVermin();
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        return vermin;
    }

    private Permanent addReadyVermin() {
        Permanent perm = new Permanent(new DiseasedVermin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
