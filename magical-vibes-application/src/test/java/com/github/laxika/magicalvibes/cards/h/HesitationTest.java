package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.IntruderAlarm;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hesitation.class, IntruderAlarm.class, VolrathsStronghold.class})
class HesitationTest extends BaseCardTest {

    @Test
    @DisplayName("When any player casts a spell, Hesitation sacrifices itself and counters that spell")
    void sacrificesAndCountersAnyPlayersSpell() {
        harness.addToBattlefield(player1, new Hesitation());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new IntruderAlarm(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hesitation");
        harness.assertInGraveyard(player1, "Hesitation");
        harness.assertInGraveyard(player2, "Intruder Alarm");
        harness.assertNotOnBattlefield(player2, "Intruder Alarm");
    }

    @Test
    @DisplayName("When its controller casts a spell, Hesitation sacrifices itself and counters that spell")
    void sacrificesAndCountersControllersSpell() {
        harness.addToBattlefield(player1, new Hesitation());

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new IntruderAlarm(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hesitation");
        harness.assertInGraveyard(player1, "Hesitation");
        harness.assertInGraveyard(player1, "Intruder Alarm");
        harness.assertNotOnBattlefield(player1, "Intruder Alarm");
    }

    @Test
    @DisplayName("Playing a land does not trigger Hesitation")
    void playingLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Hesitation());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VolrathsStronghold()));

        harness.playLand(player1, 0);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hesitation");
        harness.assertOnBattlefield(player1, "Volrath's Stronghold");
    }
}
