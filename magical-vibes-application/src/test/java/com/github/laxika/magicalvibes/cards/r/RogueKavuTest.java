package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RogueKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone puts the trigger on the stack")
    void attackingAlonePutsTriggerOnStack() {
        addCreatureReady(player1, new RogueKavu());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Rogue Kavu");
    }

    @Test
    @DisplayName("Attacking alone — 1/1 becomes 3/1 until end of turn")
    void attackingAloneBoosts() {
        Permanent kavu = addCreatureReady(player1, new RogueKavu());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent kavu = addCreatureReady(player1, new RogueKavu());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with another creature — trigger does not fire and P/T stays 1/1")
    void attackingWithOtherCreatureNoTrigger() {
        Permanent kavu = addCreatureReady(player1, new RogueKavu());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Rogue Kavu"));
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
