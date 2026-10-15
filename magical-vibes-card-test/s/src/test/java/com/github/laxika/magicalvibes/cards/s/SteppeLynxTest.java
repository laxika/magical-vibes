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

        assertThat(lynx.getEffectivePower()).isEqualTo(2);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Steppe Lynx")
    void opponentLandDoesNotTrigger() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(lynx.getEffectivePower()).isEqualTo(0);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(lynx.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lynx.getEffectivePower()).isEqualTo(0);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands entering without being played trigger landfall and boosts accumulate")
    void landEntriesGiveCumulativeBoosts() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(lynx.getEffectivePower()).isEqualTo(0);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(1);
        resolveAllTriggers();
        assertThat(lynx.getEffectivePower()).isEqualTo(2);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(3);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(lynx.getEffectivePower()).isEqualTo(4);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Each Steppe Lynx gets its own boost from a land entering")
    void multipleLynxesEachGetTheirOwnBoost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(2);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A nonland entering does not trigger landfall")
    void nonlandEnteringDoesNotTrigger() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new SteppeLynx());

        harness.enterBattlefieldAndReturn(player1, new SteppeLynx());
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(lynx.getEffectivePower()).isEqualTo(0);
        assertThat(lynx.getEffectiveToughness()).isEqualTo(1);
    }
}
