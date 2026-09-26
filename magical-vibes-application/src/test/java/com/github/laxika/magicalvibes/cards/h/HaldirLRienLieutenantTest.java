package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaldirLRienLieutenant.class, LlanowarElves.class, GrizzlyBears.class})
class HaldirLRienLieutenantTest extends BaseCardTest {

    @Test
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new HaldirLRienLieutenant()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent haldir = findPermanent(player1, "Haldir, Lórien Lieutenant");
        assertThat(haldir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, haldir)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, haldir)).isEqualTo(3);
    }

    @Test
    void activatedAbilityBoostsOtherOwnElvesOnly() {
        Permanent haldir = addCreatureReady(player1, new HaldirLRienLieutenant());
        haldir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent nonElf = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, haldir)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonElf, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.VIGILANCE)).isFalse();
    }
}
