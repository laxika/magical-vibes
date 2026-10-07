package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TopanAscetic.class})
class TopanAsceticTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another creature you control gives Topan Ascetic +1/+1")
    void tappingAnotherCreatureBoostsSelf() {
        Permanent ascetic = addCreatureReady(player1, new TopanAscetic());
        Permanent helper = addCreatureReady(player1, new TopanAscetic());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ascetic);
        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, helper.getId());
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(ascetic.getPowerModifier()).isEqualTo(1);
        assertThat(ascetic.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can tap itself to pay the cost")
    void canTapItself() {
        Permanent ascetic = addCreatureReady(player1, new TopanAscetic());

        // Ascetic is the only untapped creature, so it is auto-chosen to pay the cost.
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ascetic);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(ascetic.isTapped()).isTrue();
        assertThat(ascetic.getPowerModifier()).isEqualTo(1);
        assertThat(ascetic.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ascetic = addCreatureReady(player1, new TopanAscetic());
        Permanent helper = addCreatureReady(player1, new TopanAscetic());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ascetic);
        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, helper.getId());
        harness.passBothPriorities();

        assertThat(ascetic.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ascetic.getPowerModifier()).isEqualTo(0);
        assertThat(ascetic.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate with no untapped creature to tap")
    void cannotActivateWithNoUntappedCreature() {
        Permanent ascetic = addCreatureReady(player1, new TopanAscetic());
        ascetic.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ascetic);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Ascetic can tap itself for its ability")
    void summoningSickAsceticCanTapItself() {
        Permanent ascetic = harness.addToBattlefieldAndReturn(player1, new TopanAscetic());

        harness.activateAbility(player1, 0, null, null);

        assertThat(ascetic.isTapped()).isTrue();
        assertThat(ascetic.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(ascetic.getPowerModifier()).isEqualTo(1);
        assertThat(ascetic.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Ascetic can tap a summoning-sick creature and accumulate boosts")
    void tappedAsceticCanActivateAgainUsingSummoningSickCreature() {
        Permanent ascetic = addCreatureReady(player1, new TopanAscetic());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent helper = harness.addToBattlefieldAndReturn(player1, new TopanAscetic());
        harness.activateAbility(player1, 0, null, null);

        assertThat(helper.isTapped()).isTrue();
        assertThat(ascetic.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(ascetic.getPowerModifier()).isEqualTo(2);
        assertThat(ascetic.getToughnessModifier()).isEqualTo(2);
        assertThat(helper.getPowerModifier()).isZero();
        assertThat(helper.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's untapped creature cannot pay the cost")
    void opponentsCreatureCannotPayCost() {
        Permanent ascetic = addCreatureReady(player1, new TopanAscetic());
        ascetic.tap();
        Permanent opponent = addCreatureReady(player2, new TopanAscetic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponent.isTapped()).isFalse();
        assertThat(ascetic.getPowerModifier()).isZero();
    }
}
