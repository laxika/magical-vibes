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

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));
        harness.passBothPriorities();

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

    @Test
    @DisplayName("Each landfall adds another independent draw trigger to the only library land")
    void repeatedLandfallGrantsMultipleDrawTriggers() {
        Card targetLand = new Forest();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(targetLand, firstDraw, secondDraw));
        addCreatureReady(player1, new AmbassadorOfEvendo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1));
        Card modifiedLand = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of());
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, modifiedLand);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("An opponent's land entering does not grant an ability")
    void opponentLandDoesNotTriggerLandfall() {
        Card targetLand = new Forest();
        harness.setLibrary(player1, List.of(targetLand));
        addCreatureReady(player1, new AmbassadorOfEvendo());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(targetLand);
    }

    @Test
    @DisplayName("Tapping another land does not draw a card")
    void grantedAbilityOnlyTriggersForModifiedLand() {
        Card targetLand = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(targetLand, new GrizzlyBears()));
        addCreatureReady(player1, new AmbassadorOfEvendo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent unmodifiedLand = harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1));
        Card modifiedLand = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, modifiedLand);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(unmodifiedLand));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The granted ability draws for the land's current controller without the Ambassador")
    void grantedAbilityWorksForNewControllerWithoutAmbassador() {
        Card targetLand = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(targetLand));
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new AmbassadorOfEvendo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1));
        Card modifiedLand = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player2, modifiedLand);

        harness.tapPermanent(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
