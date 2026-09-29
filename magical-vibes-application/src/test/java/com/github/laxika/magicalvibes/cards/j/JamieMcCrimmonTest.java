package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JamieMcCrimmon.class, Millstone.class, GrizzlyBears.class})
class JamieMcCrimmonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a historic artifact boosts Jamie by its mana value")
    void historicSpellBoostsByManaValue() {
        Permanent jamie = addJamie();

        harness.setHand(player1, List.of(new Millstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a nonhistoric spell does not boost Jamie")
    void nonHistoricSpellDoesNotTrigger() {
        Permanent jamie = addJamie();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jamie loses the boost at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent jamie = addJamie();

        harness.setHand(player1, List.of(new Millstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jamie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jamie)).isEqualTo(2);
    }

    private Permanent addJamie() {
        return harness.addToBattlefieldAndReturn(player1, new JamieMcCrimmon());
    }
}
