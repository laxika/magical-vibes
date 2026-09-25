package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornscapeApprentice.class, ThornscapeMaster.class, Forest.class})
class ThornscapeApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability grants first strike to the target creature until end of turn")
    void redAbilityGrantsFirstStrike() {
        addReadyApprentice();
        Permanent target = addCreatureReady(player2, new ThornscapeMaster());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Red ability's first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addReadyApprentice();
        Permanent target = addCreatureReady(player2, new ThornscapeMaster());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("White ability taps the target creature")
    void whiteAbilityTapsTarget() {
        addReadyApprentice();
        Permanent target = addCreatureReady(player2, new ThornscapeMaster());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap cost prevents activating the other ability while the source is tapped")
    void tapCostPreventsSecondActivation() {
        Permanent apprentice = addReadyApprentice();
        Permanent target = addCreatureReady(player2, new ThornscapeMaster());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both abilities require a creature target")
    void nonCreatureTargetIsRejected() {
        addReadyApprentice();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyApprentice() {
        return addCreatureReady(player1, new ThornscapeApprentice());
    }
}
