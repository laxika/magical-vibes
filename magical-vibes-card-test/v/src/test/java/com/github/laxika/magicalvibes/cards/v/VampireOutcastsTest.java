package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireOutcasts.class})
class VampireOutcastsTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 2: enters with two +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodthirst 2: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 2 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst always adds two counters regardless of the amount of damage")
    void bloodthirstCounterCountIsFixed() {
        gd.recordDamageToPlayer(player2.getId(), 7);
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Life loss without damage does not enable bloodthirst")
    void lifeLossDoesNotEnableBloodthirst() {
        harness.setLife(player2, 17);
        castOutcasts();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst applies without casting and uses the entering creature's controller")
    void bloodthirstAppliesToNoncastEntryForOtherController() {
        gd.recordDamageToPlayer(player1.getId(), 1);

        var outcasts = harness.enterBattlefieldAndReturn(player2, new VampireOutcasts());

        assertThat(outcasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage grants lifelink and enables bloodthirst for a subsequent creature")
    void combatDamageGainsLifeAndEnablesBloodthirst() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VampireOutcasts());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        var nextOutcasts = harness.enterBattlefieldAndReturn(player1, new VampireOutcasts());
        assertThat(nextOutcasts.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lifelink gains full boosted damage to a blocker, including damage beyond lethal")
    void lifelinkGainsFullDamageToBlocker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.recordDamageToPlayer(player2.getId(), 1);
        castOutcasts();
        findPermanent(player1, "Vampire Outcasts").setSummoningSick(false);
        harness.addToBattlefield(player2, new VampireOutcasts());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 22);
        harness.assertOnBattlefield(player1, "Vampire Outcasts");
        harness.assertNotOnBattlefield(player2, "Vampire Outcasts");
        harness.assertInGraveyard(player2, "Vampire Outcasts");
    }

    private void castOutcasts() {
        harness.setHand(player1, List.of(new VampireOutcasts()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
