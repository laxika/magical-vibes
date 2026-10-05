package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurkDwellers.class, GrizzlyBears.class})
class MurkDwellersTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked attacker gets +2/+0")
    void unblockedGetsBoost() {
        Permanent dwellers = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);
        addCreatureReady(player2, new GrizzlyBears()); // a potential blocker that declines to block

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of()); // no blocks — Murk Dwellers is unblocked
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore + 2);
    }

    @Test
    @DisplayName("A blocked attacker does not get the boost")
    void blockedGetsNoBoost() {
        Permanent dwellers = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("The +2/+0 wears off at end of combat")
    void boostWearsOffAtEndOfCombat() {
        Permanent dwellers = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("The +2/+0 affects combat damage but does not persist after combat")
    void boostAffectsCombatDamageButDoesNotPersist() {
        Permanent dwellers = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 20 - powerBefore - 2);
        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("Only the unblocked Murk Dwellers gets the boost")
    void onlyUnblockedCopyGetsBoost() {
        Permanent blocked = addCreatureReady(player1, new MurkDwellers());
        Permanent unblocked = addCreatureReady(player1, new MurkDwellers());
        Permanent nonattacker = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, unblocked);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, blocked)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectivePower(gd, unblocked)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("Murk Dwellers gets its boost when player two attacks")
    void boostsWhenOtherPlayerAttacks() {
        Permanent dwellers = addCreatureReady(player2, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player1, 20 - powerBefore - 2);
        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore);
    }
}
