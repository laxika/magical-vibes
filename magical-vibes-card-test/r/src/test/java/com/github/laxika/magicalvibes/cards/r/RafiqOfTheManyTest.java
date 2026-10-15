package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RafiqOfTheMany.class, CylianElf.class})
class RafiqOfTheManyTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted and double strike trigger as two separate abilities")
    void soloAttackCreatesSeparateTriggers() {
        addCreatureReady(player1, new RafiqOfTheMany());
        addCreatureReady(player1, new CylianElf());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's solo attacker receives neither bonus")
    void opponentAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new RafiqOfTheMany());
        Permanent elf = addCreatureReady(player2, new CylianElf());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(elf.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Unblocked solo attacker deals damage in both combat damage steps")
    void doubleStrikeDealsCombatDamageTwice() {
        addCreatureReady(player1, new RafiqOfTheMany());
        addCreatureReady(player1, new CylianElf());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Ally attacking alone gets +1/+1 and double strike")
    void allyAttackingAloneBoostedAndDoubleStrike() {
        addCreatureReady(player1, new RafiqOfTheMany());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1)); // Cylian Elf attacks alone
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
        assertThat(elf.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Rafiq attacking alone boosts itself and gains double strike")
    void selfAttackingAloneBoostedAndDoubleStrike() {
        Permanent rafiq = addCreatureReady(player1, new RafiqOfTheMany());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, rafiq)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rafiq)).isEqualTo(4);
        assertThat(rafiq.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike and boost wear off at end of turn")
    void wearsOff() {
        addCreatureReady(player1, new RafiqOfTheMany());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(elf.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(elf.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new RafiqOfTheMany());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Rafiq of the Many"));
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(elf.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
