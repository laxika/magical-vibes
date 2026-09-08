package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FerocityTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature blocks, Ferocity may put a +1/+1 counter on it")
    void blocksPutsCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        enchantFerocity(creature);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveMay(true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When enchanted creature becomes blocked, Ferocity may put a +1/+1 counter on it")
    void becomesBlockedPutsCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        enchantFerocity(creature);
        creature.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveMay(true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining Ferocity's trigger puts no counter on the enchanted creature")
    void decliningTriggerPutsNoCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        enchantFerocity(creature);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveMay(false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void enchantFerocity(Permanent creature) {
        harness.setHand(player1, List.of(new Ferocity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void resolveMay(boolean accept) {
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
