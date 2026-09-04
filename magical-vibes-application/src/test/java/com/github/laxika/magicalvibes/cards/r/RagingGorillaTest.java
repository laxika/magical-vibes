package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagingGorilla.class, PhyrexianWalker.class})
class RagingGorillaTest extends BaseCardTest {

    @Test
    @DisplayName("When Raging Gorilla becomes blocked, it gets +2/-2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent gorilla = addCreatureReady(player1, new RagingGorilla());
        gorilla.setAttacking(true);
        addCreatureReady(player2, new PhyrexianWalker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gorilla.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Raging Gorilla becomes blocked by multiple creatures, it gets only one boost")
    void becomesBlockedByMultipleCreaturesGetsOneBoost() {
        Permanent gorilla = addCreatureReady(player1, new RagingGorilla());
        gorilla.setAttacking(true);
        addCreatureReady(player2, new PhyrexianWalker());
        addCreatureReady(player2, new PhyrexianWalker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gorilla.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Raging Gorilla blocks, it gets +2/-2 until end of turn")
    void blocksGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new PhyrexianWalker());
        attacker.setAttacking(true);
        Permanent gorilla = addCreatureReady(player2, new RagingGorilla());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gorilla.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Raging Gorilla blocks multiple creatures, it gets only one boost")
    void blocksMultipleCreaturesGetsOneBoost() {
        Permanent firstAttacker = addCreatureReady(player1, new PhyrexianWalker());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new PhyrexianWalker());
        secondAttacker.setAttacking(true);

        RagingGorilla card = new RagingGorilla();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent gorilla = addCreatureReady(player2, card);

        prepareDeclareBlockers();
        int gorillaIndex = gd.playerBattlefields.get(player2.getId()).indexOf(gorilla);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gorillaIndex, 0),
                new BlockerAssignment(gorillaIndex, 1)));
        resolveAllTriggers();

        assertThat(gorilla.getPowerModifier()).isEqualTo(2);
        assertThat(gorilla.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Raging Gorilla is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent gorilla = addCreatureReady(player1, new RagingGorilla());
        gorilla.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gorilla.getPowerModifier()).isZero();
        assertThat(gorilla.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent gorilla = addCreatureReady(player1, new RagingGorilla());
        gorilla.setAttacking(true);
        addCreatureReady(player2, new PhyrexianWalker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gorilla.getPowerModifier()).isZero();
        assertThat(gorilla.getToughnessModifier()).isZero();
    }
}
