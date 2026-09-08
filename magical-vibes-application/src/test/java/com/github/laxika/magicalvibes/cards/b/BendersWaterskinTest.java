package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BendersWaterskin.class, AngelsFeather.class})
class BendersWaterskinTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Bender's Waterskin adds one mana of the chosen color")
    void tapsForAnyColor() {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        waterskin.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(waterskin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bender's Waterskin untaps itself during another player's untap step")
    void untapsItselfDuringOtherPlayersUntapStep() {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        waterskin.tap();

        advanceToNextTurn(player1);

        assertThat(waterskin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Bender's Waterskin does not untap other artifacts it controls")
    void doesNotUntapOtherArtifacts() {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        waterskin.tap();
        otherArtifact.tap();

        advanceToNextTurn(player1);

        assertThat(waterskin.isTapped()).isFalse();
        assertThat(otherArtifact.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
