package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FightingChanceTest extends BaseCardTest {

    @Test
    @DisplayName("Flips independently for each blocking creature and prevents winning blockers' damage")
    void flipsForEachBlockingCreature() {
        Permanent firstAttacker = addCreatureReady(player1, new HillGiant());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new HillGiant());
        secondAttacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        prepareSpell();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<String> flips = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fighting Chance"))
                .toList();
        long losses = flips.stream().filter(log -> log.contains(" loses the coin flip")).count();
        assertThat(flips).hasSize(2);

        resolveCombat();

        assertThat(firstAttacker.getMarkedDamage() + secondAttacker.getMarkedDamage())
                .isEqualTo(losses * 2);
    }

    @Test
    @DisplayName("A combat with no blockers produces no coin flips")
    void noBlockersNoFlips() {
        addCreatureReady(player1, new HillGiant()).setAttacking(true);
        prepareSpell();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("coin flip for Fighting Chance"));
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new FightingChance()));
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
