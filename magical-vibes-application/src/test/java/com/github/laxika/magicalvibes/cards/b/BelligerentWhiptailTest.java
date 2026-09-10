package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelligerentWhiptail.class, Forest.class})
class BelligerentWhiptailTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Belligerent Whiptail first strike until end of turn")
    void landfallGrantsFirstStrike() {
        Permanent whiptail = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Landfall first strike wears off at end of turn")
    void landfallFirstStrikeWearsOff() {
        Permanent whiptail = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Belligerent Whiptail")
    void opponentLandDoesNotTrigger() {
        Permanent whiptail = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isFalse();
    }
}
