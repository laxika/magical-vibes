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

@CardUsed({Windreaver.class})
class WindreaverTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the first ability grants vigilance until end of turn")
    void grantsVigilanceUntilEndOfTurn() {
        addWindreaver();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent windreaver = findWindreaver();
        assertThat(gqs.hasKeyword(gd, windreaver, Keyword.VIGILANCE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, windreaver, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Resolving the second ability gives +0/+1 until end of turn")
    void boostsToughnessUntilEndOfTurn() {
        addWindreaver();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent windreaver = findWindreaver();
        assertThat(gqs.getEffectivePower(gd, windreaver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, windreaver)).isEqualTo(4);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, windreaver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, windreaver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving the third ability switches power and toughness until end of turn")
    void switchesPowerAndToughnessUntilEndOfTurn() {
        addWindreaver();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent windreaver = findWindreaver();
        assertThat(gqs.getEffectivePower(gd, windreaver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, windreaver)).isEqualTo(1);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, windreaver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, windreaver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving the fourth ability returns Windreaver to its owner's hand")
    void returnsToOwnersHand() {
        addWindreaver();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Windreaver");
        harness.assertNotOnBattlefield(player1, "Windreaver");
    }

    private void addWindreaver() {
        harness.addToBattlefield(player1, new Windreaver());
    }

    private Permanent findWindreaver() {
        return findPermanent(player1, "Windreaver");
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
