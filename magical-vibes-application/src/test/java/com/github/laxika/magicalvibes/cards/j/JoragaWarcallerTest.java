package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JoragaWarcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters when not multikicked")
    void entersWithoutCountersWhenNotMultikicked() {
        harness.setHand(player1, List.of(new JoragaWarcaller()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent warcaller = findPermanent(player1, "Joraga Warcaller");
        assertThat(warcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets counters for multikicker payments and scales its Elf lord ability")
    void scalesOtherOwnElvesWithMultikickerCounters() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent ownNonElf = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new JoragaWarcaller()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{1}{G}", "{1}{G}"), false);
        harness.passBothPriorities();

        Permanent warcaller = findPermanent(player1, "Joraga Warcaller");
        assertThat(warcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonElf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }
}
