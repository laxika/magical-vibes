package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.i.InkfathomDivers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JudgeOfCurrents.class, InkfathomDivers.class, HillcomberGiant.class})
class JudgeOfCurrentsTest extends BaseCardTest {

    // "Whenever a Merfolk you control becomes tapped, you may gain 1 life."

    @Test
    @DisplayName("Tapping a Merfolk you control and accepting gains 1 life")
    void tappingControlledMerfolkAcceptGainsLife() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(merfolk);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void decliningGainsNoLife() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(merfolk);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Judge triggers on its own tap (it is a Merfolk)")
    void triggersOnOwnTap() {
        Permanent judge = harness.addToBattlefieldAndReturn(player1, new JudgeOfCurrents());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(judge);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Each controlled Merfolk tap creates a separate life-gain trigger")
    void eachControlledMerfolkTapTriggersSeparately() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent firstMerfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());
        Permanent secondMerfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(firstMerfolk);
        tap(secondMerfolk);

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Tapping a non-Merfolk you control does not trigger")
    void tappingNonMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        tap(giant);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping a Merfolk an opponent controls does not trigger")
    void tappingOpponentMerfolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent opponentMerfolk = harness.addToBattlefieldAndReturn(player2, new InkfathomDivers());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(opponentMerfolk);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A queued trigger still gains life after Judge leaves the battlefield")
    void triggerResolvesAfterJudgeLeaves() {
        Permanent judge = harness.addToBattlefieldAndReturn(player1, new JudgeOfCurrents());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(merfolk);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, judge));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A queued trigger still gains life after the tapped Merfolk leaves")
    void triggerResolvesAfterTappedMerfolkLeaves() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(merfolk);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, merfolk));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two Judges trigger independently and their choices can differ")
    void multipleJudgesHaveIndependentChoices() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(merfolk);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping and tapping the same Merfolk again creates another trigger")
    void sameMerfolkCanTriggerAgain() {
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new InkfathomDivers());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(merfolk);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        merfolk.untap();
        tap(merfolk);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Attacking with two Merfolk creates a trigger for each tapped attacker")
    void attackingMerfolkTriggerSeparately() {
        addCreatureReady(player1, new JudgeOfCurrents());
        addCreatureReady(player1, new InkfathomDivers());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }
    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
