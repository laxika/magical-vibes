package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerrasHymnTest extends BaseCardTest {

    @Test
    void upkeepTriggerMayAddVerseCounter() {
        Permanent hymn = addReadyHymn(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hymn.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    void sacrificePreventsVerseCountersDividedAmongTargets() {
        Permanent hymn = addReadyHymn(player1);
        hymn.setCounterCount(CounterType.VERSE, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(bears.getId(), 2, player2.getId(), 1));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).doesNotContain(hymn);
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
        assertThat(harness.getGameData().playerDamagePreventionShields.getOrDefault(player2.getId(), 0))
                .isEqualTo(1);
    }

    @Test
    void preventionAssignmentsMustMatchVerseCounters() {
        Permanent hymn = addReadyHymn(player1);
        hymn.setCounterCount(CounterType.VERSE, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHymn(Player owner) {
        Permanent hymn = new Permanent(new SerrasHymn());
        hymn.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(owner.getId()).add(hymn);
        return hymn;
    }
}
