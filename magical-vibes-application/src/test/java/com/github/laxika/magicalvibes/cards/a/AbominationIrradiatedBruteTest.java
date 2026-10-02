package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HulkGammaGoliath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbominationIrradiatedBrute.class, HulkGammaGoliath.class, ArnimZolaBioFanatic.class, GrizzlyBears.class})
class AbominationIrradiatedBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters equal to combat damage from Gamma and Villain creatures")
    void putsCountersEqualToQualifyingCombatDamage() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new AbominationIrradiatedBrute());
        addAttacker(new HulkGammaGoliath());
        addAttacker(new ArnimZolaBioFanatic());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not trigger for a creature that is neither Gamma nor Villain")
    void ignoresNonQualifyingCombatDamage() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new AbominationIrradiatedBrute());
        addAttacker(new GrizzlyBears());

        resolveCombat();
        resolveAllTriggers();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }

    @Test
    @DisplayName("Abomination's own damage triggers only once despite having both qualifying types")
    void triggersOnceForItsOwnCombatDamage() {
        Permanent abomination = addCreatureReady(player1, new AbominationIrradiatedBrute());
        abomination.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Qualifying creatures controlled by the opponent do not trigger Abomination")
    void ignoresOpponentsQualifyingCombatDamage() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new AbominationIrradiatedBrute());
        Permanent attacker = addCreatureReady(player2, new HulkGammaGoliath());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Trample counts only damage dealt to the player")
    void countsOnlyTrampleDamageToPlayer() {
        Permanent abomination = addCreatureReady(player1, new AbominationIrradiatedBrute());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
