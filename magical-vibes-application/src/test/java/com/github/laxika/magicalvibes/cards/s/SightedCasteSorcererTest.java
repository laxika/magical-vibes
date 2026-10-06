package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SightedCasteSorcerer.class, CylianElf.class})
class SightedCasteSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new SightedCasteSorcerer());
        Permanent bears = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1)); // Cylian Elf attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Sorcerer attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent sorcerer = addCreatureReady(player1, new SightedCasteSorcerer());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, sorcerer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sorcerer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new SightedCasteSorcerer());
        Permanent bears = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new SightedCasteSorcerer());
        Permanent bears = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving the ability grants shroud until end of turn")
    void resolvingGrantsShroud() {
        Permanent sorcerer = addCreatureReady(player1, new SightedCasteSorcerer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, sorcerer, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud granted by ability resets at end of turn cleanup")
    void shroudResetsAtEndOfTurn() {
        Permanent sorcerer = addCreatureReady(player1, new SightedCasteSorcerer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, sorcerer, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sorcerer, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Multiple exalted abilities each boost a lone attacker")
    void multipleExaltedAbilitiesStack() {
        addCreatureReady(player1, new SightedCasteSorcerer());
        addCreatureReady(player1, new SightedCasteSorcerer());
        Permanent attacker = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Shroud does not stop exalted from boosting its lone attacker")
    void shroudDoesNotPreventExalted() {
        Permanent sorcerer = addCreatureReady(player1, new SightedCasteSorcerer());
        declareAttackers(player1, List.of(0));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, sorcerer, Keyword.SHROUD)).isTrue();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sorcerer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sorcerer)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's lone attacker does not trigger exalted")
    void opponentAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new SightedCasteSorcerer());
        Permanent attacker = addCreatureReady(player2, new CylianElf());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }
}
