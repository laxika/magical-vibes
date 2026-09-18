package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DefiantElf;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GempalmStrider.class, DefiantElf.class, FugitiveWizard.class})
class GempalmStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling gives Elf creatures +2/+2")
    void cyclingBoostsElvesEverywhere() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new DefiantElf());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new DefiantElf());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new GempalmStrider()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonElf)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Gempalm Strider");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Each cycle creates a separate Elf boost")
    void eachCycleCreatesSeparateElfBoost() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new DefiantElf());
        harness.setHand(player1, List.of(new GempalmStrider(), new GempalmStrider()));
        harness.setLibrary(player1, List.of(new FugitiveWizard(), new DefiantElf()));
        addCyclingMana(2);

        harness.activateHandAbility(player1, 0, null);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Gempalm Strider");
        harness.assertInHand(player1, "Fugitive Wizard");
        harness.assertInHand(player1, "Defiant Elf");
    }

    @Test
    @DisplayName("Cycling does not boost Elves that enter afterward")
    void cyclingDoesNotBoostLaterElves() {
        Permanent existingElf = harness.addToBattlefieldAndReturn(player1, new DefiantElf());
        harness.setHand(player1, List.of(new GempalmStrider()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent laterElf = harness.addToBattlefieldAndReturn(player2, new DefiantElf());
        assertThat(gqs.getEffectivePower(gd, existingElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existingElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterElf)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cycling's Elf boost wears off at end of turn")
    void elfBoostWearsOffAtEndOfTurn() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new DefiantElf());
        harness.setHand(player1, List.of(new GempalmStrider()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

    private void addCyclingMana() {
        addCyclingMana(1);
    }

    private void addCyclingMana(int cycles) {
        harness.addMana(player1, ManaColor.COLORLESS, 2 * cycles);
        harness.addMana(player1, ManaColor.GREEN, 2 * cycles);
    }
}
