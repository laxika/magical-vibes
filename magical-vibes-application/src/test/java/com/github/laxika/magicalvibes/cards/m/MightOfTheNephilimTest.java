package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NivixGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightOfTheNephilim.class, NivixGuildmage.class, GrizzlyBears.class, Forest.class})
class MightOfTheNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 for each of its colors")
    void boostsByColorCount() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new NivixGuildmage());
        int basePower = gqs.getEffectivePower(gd, guildmage);
        int baseToughness = gqs.getEffectiveToughness(gd, guildmage);

        castMight(guildmage);

        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(baseToughness + 4);
    }

    @Test
    @DisplayName("A monocolored creature gets +2/+2")
    void boostsMonocoloredCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        castMight(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("The boost ends at end of turn")
    void boostEndsAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        castMight(bears);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new MightOfTheNephilim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castMight(Permanent creature) {
        harness.setHand(player1, List.of(new MightOfTheNephilim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
