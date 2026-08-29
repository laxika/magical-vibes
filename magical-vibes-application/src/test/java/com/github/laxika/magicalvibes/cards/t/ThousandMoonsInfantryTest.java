package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThousandMoonsInfantry.class, GrizzlyBears.class})
class ThousandMoonsInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps itself during each other player's untap step")
    void untapsItselfDuringOpponentsUntapStep() {
        Permanent infantry = addReadyInfantry(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        infantry.tap();
        bears.tap();

        advanceToNextTurn(player1);

        assertThat(infantry.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap another permanent during an opponent's untap step")
    void doesNotUntapOtherPermanents() {
        Permanent infantry = addReadyInfantry(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        infantry.tap();
        bears.tap();

        advanceToNextTurn(player1);

        assertThat(infantry.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
    }

    private Permanent addReadyInfantry(Player player) {
        Permanent permanent = new Permanent(new ThousandMoonsInfantry());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
