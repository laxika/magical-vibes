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
        harness.passUntil(player2, TurnStep.UPKEEP);

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

    @Test
    @DisplayName("Landfall waits for resolution before granting first strike")
    void landfallUsesTheStack() {
        Permanent whiptail = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A land entering without being played triggers each controlled Whiptail")
    void landEnteringWithoutBeingPlayedTriggersEachWhiptail() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BelligerentWhiptail());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A nonland entering does not trigger landfall")
    void nonlandEnteringDoesNotTrigger() {
        Permanent whiptail = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());

        harness.enterBattlefieldAndReturn(player1, new BelligerentWhiptail());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike lasts through the end step and expires during cleanup")
    void firstStrikeLastsThroughEndStep() {
        Permanent whiptail = harness.addToBattlefieldAndReturn(player1, new BelligerentWhiptail());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, whiptail, Keyword.FIRST_STRIKE)).isFalse();
    }
}
