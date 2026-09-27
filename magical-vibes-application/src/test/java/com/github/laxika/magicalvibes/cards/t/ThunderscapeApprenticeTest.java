package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderscapeApprentice.class, ThornscapeApprentice.class, Forest.class})
class ThunderscapeApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("{B}, {T}: causes target player to lose 1 life")
    void targetPlayerLosesLife() {
        addReadyApprentice();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("{G}, {T}: target creature gets +1/+1 until end of turn")
    void boostsTargetCreature() {
        addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThornscapeApprentice());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The creature boost wears off at cleanup")
    void boostWearsOff() {
        addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThornscapeApprentice());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost ability only targets creatures")
    void boostAbilityRejectsNonCreatureTarget() {
        addReadyApprentice();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap cost prevents activating the other ability until the source is untapped")
    void tapCostPreventsSecondActivation() {
        Permanent apprentice = addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThornscapeApprentice());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyApprentice() {
        return addCreatureReady(player1, new ThunderscapeApprentice());
    }
}
