package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrideOfThePerfect.class, LlanowarElves.class, GrizzlyBears.class})
class PrideOfThePerfectTest extends BaseCardTest {

    @Test
    @DisplayName("Elves you control get +2/+0")
    void boostsOwnElves() {
        harness.addToBattlefield(player1, new PrideOfThePerfect());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost non-Elf creatures")
    void doesNotBoostNonElves() {
        harness.addToBattlefield(player1, new PrideOfThePerfect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's Elves")
    void doesNotBoostOpponentElves() {
        harness.addToBattlefield(player1, new PrideOfThePerfect());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Prides of the Perfect stack their bonuses")
    void bonusesStack() {
        harness.addToBattlefield(player1, new PrideOfThePerfect());
        harness.addToBattlefield(player1, new PrideOfThePerfect());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus is removed when Pride of the Perfect leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent pride = harness.addToBattlefieldAndReturn(player1, new PrideOfThePerfect());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(pride);

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
    }
}
