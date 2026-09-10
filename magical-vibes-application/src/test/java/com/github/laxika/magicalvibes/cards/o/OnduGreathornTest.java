package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnduGreathorn.class, Forest.class})
class OnduGreathornTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Ondu Greathorn +2/+2 until end of turn")
    void landfallBoostsOnduGreathorn() {
        Permanent greathorn = harness.addToBattlefieldAndReturn(player1, new OnduGreathorn());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(greathorn.getEffectivePower()).isEqualTo(4);
        assertThat(greathorn.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Ondu Greathorn")
    void opponentLandDoesNotTrigger() {
        Permanent greathorn = harness.addToBattlefieldAndReturn(player1, new OnduGreathorn());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(greathorn.getEffectivePower()).isEqualTo(2);
        assertThat(greathorn.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent greathorn = harness.addToBattlefieldAndReturn(player1, new OnduGreathorn());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(greathorn.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(greathorn.getEffectivePower()).isEqualTo(2);
        assertThat(greathorn.getEffectiveToughness()).isEqualTo(3);
    }
}
