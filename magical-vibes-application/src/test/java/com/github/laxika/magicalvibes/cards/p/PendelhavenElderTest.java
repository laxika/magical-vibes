package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Diminish;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PendelhavenElder.class, Diminish.class, GrizzlyBears.class, LlanowarElves.class})
class PendelhavenElderTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures that are currently 1/1, but not other creatures or opponents' creatures")
    void boostsOnlyYourOneOneCreatures() {
        Permanent elder = addCreatureReady(player1, new PendelhavenElder());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses current power and toughness and wears off at cleanup")
    void usesCurrentStatsAndExpires() {
        Permanent elder = addCreatureReady(player1, new PendelhavenElder());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
