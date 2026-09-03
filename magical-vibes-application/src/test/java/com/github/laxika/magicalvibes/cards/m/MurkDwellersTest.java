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
        dwellers.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears()); // a potential blocker that declines to block

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of()); // no blocks — Murk Dwellers is unblocked
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore + 2);
    }

    @Test
    @DisplayName("A blocked attacker does not get the boost")
    void blockedGetsNoBoost() {
        Permanent dwellers = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);
        dwellers.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("The +2/+0 wears off at end of combat")
    void boostWearsOffAtEndOfCombat() {
        Permanent dwellers = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);
        dwellers.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore);
    }

    @Test
    @DisplayName("The +2/+0 affects combat damage but does not persist after combat")
    void boostAffectsCombatDamageButDoesNotPersist() {
        Permanent dwellers = addCreatureReady(player1, new MurkDwellers());
        int powerBefore = gqs.getEffectivePower(gd, dwellers);
        dwellers.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 20 - powerBefore - 2);
        assertThat(gqs.getEffectivePower(gd, dwellers)).isEqualTo(powerBefore);
    }
}
