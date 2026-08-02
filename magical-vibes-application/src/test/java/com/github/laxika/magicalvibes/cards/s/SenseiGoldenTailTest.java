package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SenseiGoldenTailTest extends BaseCardTest {

    @Test
    @DisplayName("Training ability gives a creature a training counter, Samurai subtype, and Bushido")
    void trainsCreature() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TRAINING)).isEqualTo(1);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.SAMURAI);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(target))));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each training ability activation adds another Bushido instance")
    void repeatedTrainingAddsBushidoInstances() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        harness.passBothPriorities();
        sensei.untap();
        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(target))));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TRAINING)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Trained creature gets Bushido when it blocks")
    void trainedCreatureGetsBushidoWhenBlocking() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(target), 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
