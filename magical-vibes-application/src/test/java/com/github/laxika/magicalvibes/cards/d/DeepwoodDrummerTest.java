package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepwoodDrummer.class, Forest.class, GrizzlyBears.class})
class DeepwoodDrummerTest extends BaseCardTest {

    @Test
    void activationBoostsTargetCreatureAndDiscardsACard() {
        Permanent drummer = addCreatureReady(player1, new DeepwoodDrummer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
        assertThat(drummer.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DeepwoodDrummer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness);
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new DeepwoodDrummer());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new DeepwoodDrummer());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
