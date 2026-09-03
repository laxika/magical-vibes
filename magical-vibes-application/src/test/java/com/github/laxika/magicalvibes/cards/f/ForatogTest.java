package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Foratog.class, Forest.class, Island.class})
class ForatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest gives Foratog +2/+2")
    void sacrificingForestBoostsForatog() {
        Permanent foratog = addCreatureReady(player1, new Foratog());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(foratog.getEffectivePower()).isEqualTo(3);
        assertThat(foratog.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Can activate multiple times, sacrificing multiple Forests")
    void canActivateMultipleTimes() {
        Permanent foratog = addCreatureReady(player1, new Foratog());
        Permanent forest1 = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Two Forests present → prompted which to sacrifice.
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, forest1.getId());
        harness.passBothPriorities();
        // Only one Forest left → auto-sacrificed.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(foratog.getEffectivePower()).isEqualTo(5);
        assertThat(foratog.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent foratog = addCreatureReady(player1, new Foratog());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(foratog.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(foratog.getEffectivePower()).isEqualTo(1);
        assertThat(foratog.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without the {G} mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Foratog());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        // No mana added.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a Forest to sacrifice")
    void cannotActivateWithoutForest() {
        addCreatureReady(player1, new Foratog());
        harness.addToBattlefield(player1, new Island());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Island");
    }
}
