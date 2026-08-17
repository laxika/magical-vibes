package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HammerDropperTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on a lesser-power attacking creature")
    void mentorCountersLesserPowerAttacker() {
        Permanent hammerDropper = addCreatureReady(player1, new HammerDropper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(hammerDropper, attacker);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void declareAttackers(Permanent first, Permanent second) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
    }
}
