package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopanAsceticTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another creature you control gives Topan Ascetic +1/+1")
    void tappingAnotherCreatureBoostsSelf() {
        Permanent ascetic = addReady(player1, new TopanAscetic());
        Permanent bears = addReady(player1, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ascetic);
        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(ascetic.getPowerModifier()).isEqualTo(1);
        assertThat(ascetic.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can tap itself to pay the cost")
    void canTapItself() {
        Permanent ascetic = addReady(player1, new TopanAscetic());

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
        Permanent ascetic = addReady(player1, new TopanAscetic());
        Permanent bears = addReady(player1, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ascetic);
        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
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
        Permanent ascetic = addReady(player1, new TopanAscetic());
        ascetic.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ascetic);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
