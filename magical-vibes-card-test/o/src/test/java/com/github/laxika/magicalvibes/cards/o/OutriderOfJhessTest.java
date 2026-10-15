package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed({OutriderOfJhess.class, CylianElf.class, DeftDuelist.class})
class OutriderOfJhessTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new OutriderOfJhess());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1)); // Cylian Elf attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Outrider attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent outrider = addCreatureReady(player1, new OutriderOfJhess());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new OutriderOfJhess());
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
        addCreatureReady(player1, new OutriderOfJhess());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Outrider of Jhess"));
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Outrider gives the lone attacker its own exalted bonus")
    void multipleOutridersBoostTheSameAttacker() {
        Permanent first = addCreatureReady(player1, new OutriderOfJhess());
        Permanent second = addCreatureReady(player1, new OutriderOfJhess());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opponentsLoneAttackerIsNotBoosted() {
        addCreatureReady(player1, new OutriderOfJhess());
        Permanent elf = addCreatureReady(player2, new CylianElf());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted resolves after its source leaves the battlefield")
    void sourceLeavingDoesNotStopTheBonus() {
        Permanent outrider = addCreatureReady(player1, new OutriderOfJhess());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, outrider));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Outrider of Jhess");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boosts a lone attacker with shroud without targeting it")
    void shroudDoesNotPreventExalted() {
        addCreatureReady(player1, new OutriderOfJhess());
        Permanent duelist = addCreatureReady(player1, new DeftDuelist());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duelist)).isEqualTo(2);
    }
}
