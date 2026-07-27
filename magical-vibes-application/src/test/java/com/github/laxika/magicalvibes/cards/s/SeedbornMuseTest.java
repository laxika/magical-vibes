package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeedbornMuseTest extends BaseCardTest {

    

    @Test
    @DisplayName("Seedborn Muse untaps all your permanents during opponent's untap step")
    void untapsAllYourPermanentsOnOpponentsUntapStep() {
        Permanent muse = addReadySeedbornMuse(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        muse.tap();
        bears.tap();
        assertThat(muse.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();

        advanceToNextTurn(player1); // next active is player2

        assertThat(muse.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Without Seedborn Muse, non-active player's tapped permanents stay tapped")
    void withoutSeedbornMusePermanentsStayTapped() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        assertThat(bears.isTapped()).isTrue();

        advanceToNextTurn(player1); // next active is player2

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Seedborn Muse only untaps permanents its controller controls")
    void onlyControllerPermanentsUntap() {
        Permanent p1Muse = addReadySeedbornMuse(player1);
        Permanent p1Bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent p2Bears = addCreatureReady(player2, new GrizzlyBears());

        p1Muse.tap();
        p1Bears.tap();
        p2Bears.tap();

        advanceToNextTurn(player1); // player2 untap step

        assertThat(p1Muse.isTapped()).isFalse();
        assertThat(p1Bears.isTapped()).isFalse();
        assertThat(p2Bears.isTapped()).isFalse(); // active player's normal untap
    }

    private Permanent addReadySeedbornMuse(Player player) {
        Permanent perm = new Permanent(new SeedbornMuse());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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
