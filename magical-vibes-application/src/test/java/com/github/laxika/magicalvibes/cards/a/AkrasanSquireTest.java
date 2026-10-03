package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.d.DeftDuelist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkrasanSquire.class, CylianElf.class, DeftDuelist.class})
class AkrasanSquireTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new AkrasanSquire());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1)); // Cylian Elf attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Squire attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent squire = addCreatureReady(player1, new AkrasanSquire());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, squire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squire)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new AkrasanSquire());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new AkrasanSquire());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Akrasan Squire"));
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Squire gives the lone attacker its own exalted bonus")
    void multipleSquiresBoostTheSameAttacker() {
        Permanent firstSquire = addCreatureReady(player1, new AkrasanSquire());
        Permanent secondSquire = addCreatureReady(player1, new AkrasanSquire());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, firstSquire)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondSquire)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opponentsLoneAttackerIsNotBoosted() {
        addCreatureReady(player1, new AkrasanSquire());
        Permanent elf = addCreatureReady(player2, new CylianElf());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted still resolves after its source leaves the battlefield")
    void sourceLeavingDoesNotStopTheBonus() {
        Permanent squire = addCreatureReady(player1, new AkrasanSquire());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, squire));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Akrasan Squire");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing one of two attackers does not retroactively trigger exalted")
    void remainingAttackerDoesNotReceiveABonus() {
        Permanent squire = addCreatureReady(player1, new AkrasanSquire());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(0, 1));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, squire));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boosts a lone attacker with shroud without targeting it")
    void shroudDoesNotPreventExalted() {
        addCreatureReady(player1, new AkrasanSquire());
        Permanent duelist = addCreatureReady(player1, new DeftDuelist());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duelist)).isEqualTo(2);
    }
}
