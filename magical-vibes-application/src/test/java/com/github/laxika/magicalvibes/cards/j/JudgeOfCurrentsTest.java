package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.i.InkfathomDivers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
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

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Judge triggers on its own tap (it is a Merfolk)")
    void triggersOnOwnTap() {
        Permanent judge = harness.addToBattlefieldAndReturn(player1, new JudgeOfCurrents());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(judge);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
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

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
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

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
