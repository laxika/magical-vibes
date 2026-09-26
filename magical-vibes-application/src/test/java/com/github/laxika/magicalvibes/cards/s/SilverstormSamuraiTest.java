package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverstormSamurai.class, BileUrchin.class})
class SilverstormSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("When Silverstorm Samurai becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SilverstormSamurai());
        samurai.setAttacking(true);
        addCreatureReady(player2, new BileUrchin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Silverstorm Samurai blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new BileUrchin());
        attacker.setAttacking(true);
        Permanent samurai = addCreatureReady(player2, new SilverstormSamurai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Silverstorm Samurai is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent samurai = addCreatureReady(player1, new SilverstormSamurai());
        samurai.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(samurai.getPowerModifier()).isZero();
        assertThat(samurai.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bushido triggers only once when Silverstorm Samurai is blocked by multiple creatures")
    void multipleBlockersTriggerBushidoOnce() {
        Permanent samurai = addCreatureReady(player1, new SilverstormSamurai());
        samurai.setAttacking(true);
        addCreatureReady(player2, new BileUrchin());
        addCreatureReady(player2, new BileUrchin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Bushido bonus wears off at end of turn")
    void bushidoWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new BileUrchin());
        attacker.setAttacking(true);
        Permanent samurai = addCreatureReady(player2, new SilverstormSamurai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isEqualTo(1);
        assertThat(samurai.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(samurai.getPowerModifier()).isZero();
        assertThat(samurai.getToughnessModifier()).isZero();
    }
}
