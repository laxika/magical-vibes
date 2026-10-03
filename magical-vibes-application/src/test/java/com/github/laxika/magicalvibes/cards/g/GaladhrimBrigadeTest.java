package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaladhrimBrigade.class, GrizzlyBears.class, LlanowarElves.class})
class GaladhrimBrigadeTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new GaladhrimBrigade()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Galadhrim Brigade")).hasSize(2);
        assertThat(findPermanents(player1, "Galadhrim Brigade"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Other Elves you control get +1/+1")
    void boostsOtherControlledElves() {
        Permanent brigade = harness.addToBattlefieldAndReturn(player1, new GaladhrimBrigade());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, brigade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brigade)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonElf)).isEqualTo(2);
    }
}
