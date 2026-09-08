package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KurosTakenTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido gives Kuros's Taken +1/+1 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent taken = addCreatureReady(player1, new KurosTaken());
        taken.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, taken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, taken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido gives Kuros's Taken +1/+1 when it blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent taken = addCreatureReady(player2, new KurosTaken());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, taken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, taken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating {1}{B} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent taken = addCreatureReady(player1, new KurosTaken());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(taken.getRegenerationShield()).isEqualTo(1);
    }
}
