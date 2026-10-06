package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FireNavyTrebuchet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RogueKavu.class, GrizzlyBears.class, FireNavyTrebuchet.class})
class RogueKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone puts the trigger on the stack")
    void attackingAlonePutsTriggerOnStack() {
        addCreatureReady(player1, new RogueKavu());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
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
    @DisplayName("Attacks-alone trigger still resolves after another creature enters attacking")
    void attackingAloneTriggerDoesNotRecheckAloneAtResolution() {
        addCreatureReady(player1, new FireNavyTrebuchet());
        Permanent kavu = addCreatureReady(player1, new RogueKavu());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(1));
            harness.passBothPriorities(); // resolve Fire Navy Trebuchet's token trigger
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities(); // resolve Rogue Kavu's attack-alone trigger

            assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent kavu = addCreatureReady(player1, new RogueKavu());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with another creature — trigger does not fire and P/T stays 1/1")
    void attackingWithOtherCreatureNoTrigger() {
        Permanent kavu = addCreatureReady(player1, new RogueKavu());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another creature attacking alone does not trigger Rogue Kavu")
    void anotherCreatureAttackingAloneDoesNotTrigger() {
        Permanent kavu = addCreatureReady(player1, new RogueKavu());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Rogue Kavus attacking together trigger neither ability")
    void twoRogueKavusAttackingTogetherDoNotTrigger() {
        Permanent first = addCreatureReady(player1, new RogueKavu());
        Permanent second = addCreatureReady(player1, new RogueKavu());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
    }
}
