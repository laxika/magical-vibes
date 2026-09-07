package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flatten.class, ColossalDreadmaw.class, FountainOfYouth.class, GrizzlyBears.class})
class FlattenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature -4/-4 until end of turn")
    void resolvingGivesMinusFourMinusFour() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, dreadmaw.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dreadmaw)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dreadmaw)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature with toughness 2 dies to the -4 toughness")
    void twoToughnessCreatureDies() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOff() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, dreadmaw.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dreadmaw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dreadmaw)).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
