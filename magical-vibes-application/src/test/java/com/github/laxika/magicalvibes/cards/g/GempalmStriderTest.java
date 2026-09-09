package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkwatchElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GempalmStrider.class, DarkwatchElves.class, GrizzlyBears.class})
class GempalmStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling gives Elf creatures +2/+2")
    void cyclingBoostsElvesEverywhere() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new DarkwatchElves());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new DarkwatchElves());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GempalmStrider()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonElf)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Gempalm Strider");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling's Elf boost wears off at end of turn")
    void elfBoostWearsOffAtEndOfTurn() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new DarkwatchElves());
        harness.setHand(player1, List.of(new GempalmStrider()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
