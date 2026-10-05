package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerrowCommerce.class, DeeptreadMerrow.class, HillcomberGiant.class})
class MerrowCommerceTest extends BaseCardTest {

    private void reachEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void resolveEndStepTrigger(Player activePlayer) {
        reachEndStep(activePlayer);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
    }

    @Test
    @DisplayName("Untaps all Merfolk you control at the beginning of your end step")
    void untapsControlledMerfolk() {
        harness.addToBattlefield(player1, new MerrowCommerce());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        merfolk.tap();

        resolveEndStepTrigger(player1);

        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap non-Merfolk creatures you control")
    void leavesNonMerfolkTapped() {
        harness.addToBattlefield(player1, new MerrowCommerce());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());
        giant.tap();

        resolveEndStepTrigger(player1);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps all controlled Merfolk and leaves opposing Merfolk tapped")
    void untapsAllControlledMerfolkOnly() {
        Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());
        Permanent firstMerfolk = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        Permanent secondMerfolk = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        Permanent nonMerfolk = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());
        Permanent opposingMerfolk = harness.addToBattlefieldAndReturn(player2, new DeeptreadMerrow());

        commerce.tap();
        firstMerfolk.tap();
        secondMerfolk.tap();
        nonMerfolk.tap();
        opposingMerfolk.tap();

        resolveEndStepTrigger(player1);

        assertThat(commerce.isTapped()).isFalse();
        assertThat(firstMerfolk.isTapped()).isFalse();
        assertThat(secondMerfolk.isTapped()).isFalse();
        assertThat(nonMerfolk.isTapped()).isTrue();
        assertThat(opposingMerfolk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps Merfolk that entered after the end-step ability triggered")
    void checksControlledMerfolkWhenTriggerResolves() {
        harness.addToBattlefield(player1, new MerrowCommerce());

        reachEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        merfolk.tap();
        assertThat(merfolk.isTapped()).isTrue();

        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(merfolk.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new MerrowCommerce());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        merfolk.tap();

        reachEndStep(player2);

        assertThat(merfolk.isTapped()).isTrue();
    }
}
