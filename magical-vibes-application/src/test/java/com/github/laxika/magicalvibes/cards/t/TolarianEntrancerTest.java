package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.c.ChokingVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolarianEntrancer.class, BenalishKnight.class, ChokingVines.class})
class TolarianEntrancerTest extends BaseCardTest {

    @Test
    @DisplayName("At end of combat, controller gains control of each blocking creature")
    void gainsControlOfBlockersAtEndOfCombat() {
        Permanent entrancer = addCreatureReady(player1, new TolarianEntrancer());
        entrancer.setAttacking(true);
        Permanent knight1 = addCreatureReady(player2, new BenalishKnight());
        Permanent knight2 = addCreatureReady(player2, new BenalishKnight());

        declareBlockers(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0));

        // Control changes only when combat ends.
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight1, knight2);

        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight1, knight2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(knight1, knight2);
    }

    @Test
    @DisplayName("Control is permanent and survives the Entrancer leaving the battlefield")
    void controlSurvivesSourceLeaving() {
        Permanent entrancer = addCreatureReady(player1, new TolarianEntrancer());
        entrancer.setAttacking(true);
        Permanent knight = addCreatureReady(player2, new BenalishKnight());

        declareBlockers(new BlockerAssignment(0, 0));
        leaveEndOfCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, entrancer));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(knight);
    }

    @Test
    @DisplayName("Control gain still resolves if the Entrancer leaves before its trigger resolves")
    void controlGainSurvivesSourceLeavingBeforeTriggerResolution() {
        Permanent entrancer = addCreatureReady(player1, new TolarianEntrancer());
        Permanent knight = addCreatureReady(player2, new BenalishKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, entrancer));
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(knight);
    }

    @Test
    @DisplayName("Does nothing when the Entrancer is not blocked")
    void noControlWhenNotBlocked() {
        addCreatureReady(player1, new TolarianEntrancer());
        Permanent knight = addCreatureReady(player2, new BenalishKnight());

        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(GainControlOfPermanentAtEndOfCombat.class)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight);
    }

    @Test
    @DisplayName("Does not trigger when an effect makes the Entrancer blocked without a creature")
    void noControlWhenBlockedWithoutCreature() {
        Permanent entrancer = addCreatureReady(player1, new TolarianEntrancer());
        addCreatureReady(player2, new BenalishKnight());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        harness.setHand(player2, List.of(new ChokingVines()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstantForX(player2, 0, 1, List.of(entrancer.getId()));
        harness.passBothPriorities();

        assertThat(entrancer.isBlockedWithoutBlockers()).isTrue();
        assertThat(gd.hasDelayedAction(GainControlOfPermanentAtEndOfCombat.class)).isFalse();
    }

    private void declareBlockers(BlockerAssignment... assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(assignments));
        harness.passBothPriorities();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
