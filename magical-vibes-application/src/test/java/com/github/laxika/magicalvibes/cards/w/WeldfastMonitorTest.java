package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeldfastMonitor.class})
class WeldfastMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("{R} grants Weldfast Monitor menace until end of turn")
    void grantsMenaceUntilEndOfTurn() {
        Permanent monitor = addCreatureReady(player1, new WeldfastMonitor());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gqs.hasKeyword(gd, monitor, Keyword.MENACE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, monitor, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, monitor, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Weldfast Monitor can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent monitor = harness.addToBattlefieldAndReturn(player1, new WeldfastMonitor());
        monitor.setSummoningSick(true);
        monitor.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, monitor, Keyword.MENACE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, monitor, Keyword.MENACE)).isTrue();
        assertThat(monitor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability grants menace only to the Monitor that activated it")
    void grantsMenaceOnlyToSource() {
        Permanent source = addCreatureReady(player1, new WeldfastMonitor());
        Permanent other = addCreatureReady(player1, new WeldfastMonitor());
        Permanent opponent = addCreatureReady(player2, new WeldfastMonitor());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.MENACE)).isFalse();
    }
}
