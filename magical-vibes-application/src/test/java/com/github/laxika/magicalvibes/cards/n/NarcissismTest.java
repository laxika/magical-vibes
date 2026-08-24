package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed({Narcissism.class, Forest.class, GrizzlyBears.class})
class NarcissismTest extends BaseCardTest {

    @Test
    void discardingACardBoostsTargetCreature() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void sacrificingNarcissismBoostsTargetCreature() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
        harness.assertInGraveyard(player1, "Narcissism");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
    }

    @Test
    void cannotActivateDiscardAbilityWithoutACardInHand() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        addNarcissism();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addNarcissism() {
        harness.addToBattlefield(player1, new Narcissism());
    }
}
