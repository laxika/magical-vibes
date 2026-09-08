package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarDanceTest extends BaseCardTest {

    @Test
    void upkeepTriggerMayAddVerseCounter() {
        Permanent warDance = addWarDance(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(warDance.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    void upkeepTriggerMayBeDeclined() {
        Permanent warDance = addWarDance(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(warDance.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    void sacrificingWarDanceBoostsTargetByVerseCounters() {
        Permanent warDance = addWarDance(player1);
        warDance.setCounterCount(CounterType.VERSE, 3);
        Permanent target = addBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "War Dance");
    }

    private Permanent addWarDance(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new WarDance());
    }

    private Permanent addBears(Player owner) {
        Permanent perm = harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }
}
