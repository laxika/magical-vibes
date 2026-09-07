package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelligerentOfTheBall.class, Forest.class, GrizzlyBears.class})
class BelligerentOfTheBallTest extends BaseCardTest {

    @Test
    @DisplayName("Celebration boosts a target creature and grants menace")
    void celebrationBoostsAndGrantsMenace() {
        castBelligerentOfTheBall();
        Permanent bear = castGrizzlyBears();

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Celebration does not trigger without two nonland permanents")
    void doesNotTriggerWithoutTwoNonlandPermanents() {
        castBelligerentOfTheBall();
        harness.addToBattlefield(player1, new Forest());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger only offers creatures you control")
    void onlyOffersCreaturesYouControl() {
        castBelligerentOfTheBall();
        Permanent ownBear = castGrizzlyBears();
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownBear.getId())
                .doesNotContain(opposingBear.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The temporary bonus and menace wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        castBelligerentOfTheBall();
        Permanent bear = castGrizzlyBears();

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
    }

    private void castBelligerentOfTheBall() {
        harness.setHand(player1, List.of(new BelligerentOfTheBall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Grizzly Bears");
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
