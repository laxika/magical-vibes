package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
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

@CardUsed({Dromosaur.class, GorillaWarrior.class})
class DromosaurTest extends BaseCardTest {

    @Test
    @DisplayName("When Dromosaur becomes blocked, it gets +2/-2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent dromosaur = addCreatureReady(player1, new Dromosaur());
        dromosaur.setAttacking(true);
        addCreatureReady(player2, new GorillaWarrior());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dromosaur.getPowerModifier()).isEqualTo(2);
        assertThat(dromosaur.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Dromosaur blocks, it gets +2/-2 until end of turn")
    void blocksGetsBoost() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        Permanent dromosaur = addCreatureReady(player2, new Dromosaur());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dromosaur.getPowerModifier()).isEqualTo(2);
        assertThat(dromosaur.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("When Dromosaur is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent dromosaur = addCreatureReady(player1, new Dromosaur());
        dromosaur.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(dromosaur.getPowerModifier()).isZero();
        assertThat(dromosaur.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent dromosaur = addCreatureReady(player1, new Dromosaur());
        dromosaur.setAttacking(true);
        addCreatureReady(player2, new GorillaWarrior());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dromosaur.getPowerModifier()).isZero();
        assertThat(dromosaur.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Dromosaur blocks multiple creatures, it gets +2/-2 only once")
    void blocksMultipleCreaturesGetsOneBoost() {
        Dromosaur card = new Dromosaur();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent dromosaur = addCreatureReady(player2, card);

        Permanent firstAttacker = addCreatureReady(player1, new GorillaWarrior());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new GorillaWarrior());
        secondAttacker.setAttacking(true);

        int dromosaurIndex = gd.playerBattlefields.get(player2.getId()).indexOf(dromosaur);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(dromosaurIndex, 0),
                new BlockerAssignment(dromosaurIndex, 1)
        ));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dromosaur);
        assertThat(dromosaur.getPowerModifier()).isEqualTo(2);
        assertThat(dromosaur.getToughnessModifier()).isEqualTo(-2);
    }
}
