package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoldTheLine.class, MossKami.class})
class HoldTheLineTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking creatures get +7/+7, but other creatures do not")
    void boostsBlockingCreaturesOnly() {
        Permanent attacker = addCreatureReady(player1, new MossKami());

        Permanent blocker = addCreatureReady(player2, new MossKami());
        Permanent bystander = addCreatureReady(player2, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castHoldTheLine();

        assertThat(blocker.getEffectivePower()).isEqualTo(12);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(12);
        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(5);
        assertThat(bystander.getEffectivePower()).isEqualTo(5);
        assertThat(bystander.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The blocking-creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new MossKami());
        addCreatureReady(player1, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castHoldTheLine();

        assertThat(blocker.getEffectivePower()).isEqualTo(12);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(12);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(5);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
    }

    private void castHoldTheLine() {
        harness.castFromHand(player1, new HoldTheLine(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}
