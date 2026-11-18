package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteppeLynx.class, Forest.class})
class SteppeLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Steppe Lynx +2/+2 until end of turn")
    void landfallBoostsSteppeLynx() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Steppe Lynx")
    void opponentLandDoesNotTrigger() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands entering without being played trigger landfall and boosts accumulate")
    void landEntriesGiveCumulativeBoosts() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(3);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(5);
    }

    @Test
    @DisplayName("Each Steppe Lynx gets its own boost from a land entering")
    void multipleLynxesEachGetTheirOwnBoost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("A nonland entering does not trigger landfall")
    void nonlandEnteringDoesNotTrigger() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());

        harness.enterBattlefieldAndReturn(player1, new SteppeLynx());
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lynx)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, lynx)).isEqualTo(1);
    }
}
