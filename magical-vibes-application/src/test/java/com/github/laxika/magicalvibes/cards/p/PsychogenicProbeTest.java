package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PsychogenicProbeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent who shuffles their library")
    void damagesOpponentWhoShufflesLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when its controller shuffles their own library")
    void doesNotTriggerOnOwnShuffle() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        LibraryShuffleHelper.shuffleLibrary(gd, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
