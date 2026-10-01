package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fallowsage.class, AxegrinderGiant.class})
class FallowsageTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may draw a card."

    @Test
    @DisplayName("Tapping Fallowsage and accepting draws a card")
    void tappingSelfAcceptDraws() {
        Permanent fallowsage = harness.addToBattlefieldAndReturn(player1, new Fallowsage());
        harness.setLibrary(player1, List.of(new AxegrinderGiant()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        tap(fallowsage);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger draws no card")
    void decliningDrawsNothing() {
        Permanent fallowsage = harness.addToBattlefieldAndReturn(player1, new Fallowsage());
        harness.setLibrary(player1, List.of(new AxegrinderGiant()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        tap(fallowsage);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new Fallowsage());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AxegrinderGiant());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent-controlled Fallowsage draws for its controller when tapped")
    void opponentControlledFallowsageDrawsForItsController() {
        Permanent fallowsage = harness.addToBattlefieldAndReturn(player2, new Fallowsage());
        harness.setLibrary(player2, List.of(new AxegrinderGiant()));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int ownHandBefore = gd.playerHands.get(player1.getId()).size();

        tap(fallowsage);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownHandBefore);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
