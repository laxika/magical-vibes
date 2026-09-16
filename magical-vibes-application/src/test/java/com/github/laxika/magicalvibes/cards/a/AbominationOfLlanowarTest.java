package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbominationOfLlanowar.class, LlanowarElves.class, GrizzlyBears.class})
class AbominationOfLlanowarTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness count own Elves on the battlefield and in the graveyard")
    void powerAndToughnessCountOwnElvesInBothZones() {
        Permanent abomination = addCreatureReady(player1, new AbominationOfLlanowar());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(
                new LlanowarElves(), new LlanowarElves(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));

        // The Abomination itself and one other controlled Elf, plus two Elf cards in its graveyard.
        assertThat(gqs.getEffectivePower(gd, abomination)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, abomination)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power and toughness update when the counted zones change")
    void powerAndToughnessUpdateDynamically() {
        Permanent abomination = addCreatureReady(player1, new AbominationOfLlanowar());

        assertThat(gqs.getEffectivePower(gd, abomination)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, abomination)).isEqualTo(1);

        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, abomination)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, abomination)).isEqualTo(3);
    }
}
