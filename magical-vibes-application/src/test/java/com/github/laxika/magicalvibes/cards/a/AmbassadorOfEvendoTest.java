package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbassadorOfEvendo.class, Forest.class, GrizzlyBears.class})
class AmbassadorOfEvendoTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives a random library land a perpetual tap-draw ability")
    void landfallGrantsTapDrawToLibraryLand() {
        Card playedLand = new Forest();
        Card targetLand = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(playedLand));
        harness.setLibrary(player1, List.of(targetLand, drawn));
        addCreatureReady(player1, new AmbassadorOfEvendo());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1));
        Card modifiedLand = gd.playerHands.get(player1.getId()).getFirst();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, modifiedLand);
        int handSizeBeforeTap = gd.playerHands.get(player1.getId()).size();

        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBeforeTap + 1)
                .contains(drawn);
    }

    @Test
    @DisplayName("Landfall does nothing when the library has no land")
    void landfallWithNoLibraryLandDoesNothing() {
        Card playedLand = new Forest();
        Card remainingCard = new GrizzlyBears();
        harness.setHand(player1, List.of(playedLand));
        harness.setLibrary(player1, List.of(remainingCard));
        addCreatureReady(player1, new AmbassadorOfEvendo());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }
}
