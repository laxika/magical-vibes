package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.o.OboroEnvoy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArabaMothrider.class, OboroEnvoy.class})
class ArabaMothriderTest extends BaseCardTest {

    @Test
    @DisplayName("When Araba Mothrider becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new ArabaMothrider());
        mothrider.setAttacking(true);
        addCreatureReady(player2, new OboroEnvoy());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(mothrider.getPowerModifier()).isEqualTo(1);
        assertThat(mothrider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Araba Mothrider blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new OboroEnvoy());
        attacker.setAttacking(true);
        Permanent mothrider = addCreatureReady(player2, new ArabaMothrider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(mothrider.getPowerModifier()).isEqualTo(1);
        assertThat(mothrider.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Araba Mothrider is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new ArabaMothrider());
        mothrider.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(mothrider.getPowerModifier()).isZero();
        assertThat(mothrider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent mothrider = addCreatureReady(player1, new ArabaMothrider());
        mothrider.setAttacking(true);
        addCreatureReady(player2, new OboroEnvoy());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido triggers only once when Araba Mothrider becomes blocked by multiple creatures")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent mothrider = addCreatureReady(player1, new ArabaMothrider());
        mothrider.setAttacking(true);
        addCreatureReady(player2, new OboroEnvoy());
        addCreatureReady(player2, new OboroEnvoy());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0),
                    new BlockerAssignment(1, 0)));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(2);
    }

    @Test
    @CardUsed({ArabaMothrider.class, HighGround.class, OboroEnvoy.class})
    @DisplayName("Bushido triggers only once when Araba Mothrider blocks multiple creatures")
    void blocksMultipleCreaturesGetsOneBushidoBonus() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent mothrider = addCreatureReady(player2, new ArabaMothrider());

        Permanent firstAttacker = addCreatureReady(player1, new OboroEnvoy());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new OboroEnvoy());
        secondAttacker.setAttacking(true);

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(1, 0),
                    new BlockerAssignment(1, 1)));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(2);
    }
}
