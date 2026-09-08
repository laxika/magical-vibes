package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HamatoGuardianStance.class, GrizzlyBears.class, FountainOfYouth.class})
class HamatoGuardianStanceTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts target creature, grants flying, and lets its controller scry 1")
    void boostsGrantsFlyingAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHamatoGuardianStance(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHamatoGuardianStance(target);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HamatoGuardianStance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castHamatoGuardianStance(Permanent target) {
        harness.setHand(player1, List.of(new HamatoGuardianStance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
