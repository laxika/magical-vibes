package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalthorTheStout.class, GrizzlyBears.class})
class BalthorTheStoutTest extends BaseCardTest {

    @Test
    @DisplayName("Other Barbarian creatures get +1/+1")
    void boostsOtherBarbarians() {
        addCreatureReady(player1, new BalthorTheStout());
        Permanent barbarian = addCreatureReady(player1, new BalthorTheStout());

        assertThat(gqs.getEffectivePower(gd, barbarian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, barbarian)).isEqualTo(3);
    }

    @Test
    @DisplayName("The static ability also boosts an opponent's Barbarian")
    void boostsOpponentsBarbarians() {
        addCreatureReady(player1, new BalthorTheStout());
        Permanent barbarian = addCreatureReady(player2, new BalthorTheStout());

        assertThat(gqs.getEffectivePower(gd, barbarian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, barbarian)).isEqualTo(3);
    }

    @Test
    @DisplayName("Balthor the Stout does not boost itself or non-Barbarians")
    void doesNotBoostItselfOrNonBarbarians() {
        Permanent balthor = addCreatureReady(player1, new BalthorTheStout());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, balthor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, balthor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability pumps another Barbarian until end of turn")
    void pumpsAnotherBarbarianUntilEndOfTurn() {
        addCreatureReady(player1, new BalthorTheStout());
        Permanent barbarian = addCreatureReady(player2, new BalthorTheStout());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, barbarian.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, barbarian)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barbarian)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, barbarian)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, barbarian)).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability cannot target Balthor itself")
    void cannotTargetItself() {
        Permanent balthor = addCreatureReady(player1, new BalthorTheStout());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, balthor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Barbarian creature")
    void cannotTargetNonBarbarian() {
        addCreatureReady(player1, new BalthorTheStout());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
