package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shimmer.class, Island.class, Forest.class})
class ShimmerTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private Permanent shimmerWithType(CardSubtype chosen) {
        Permanent shimmer = harness.addToBattlefieldAndReturn(player1, new Shimmer());
        shimmer.setChosenSubtype(chosen);
        return shimmer;
    }

    @Test
    @DisplayName("Resolving Shimmer awaits a land type choice")
    void resolvingAwaitsLandTypeChoice() {
        harness.castFromHand(player1, new Shimmer(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ISLAND");

        assertThat(findPermanent(player1, "Shimmer").getChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Resolving Shimmer offers nonbasic land types")
    void offersNonbasicLandTypes() {
        harness.castFromHand(player1, new Shimmer(), "{2}{U}{U}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("DESERT");
    }

    @Test
    @DisplayName("A land of the chosen type phases out during its controller's untap step")
    void chosenTypeLandPhasesOut() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        shimmerWithType(CardSubtype.ISLAND);

        harness.forceActivePlayer(player1);
        advanceTurn(); // opponent's untap step
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island);

        advanceTurn(); // controller's untap step

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(island);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island);
    }

    @Test
    @DisplayName("A land phased out by Shimmer phases in during its next untap step")
    void chosenTypeLandPhasesInOnNextUntap() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        shimmerWithType(CardSubtype.ISLAND);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(island);

        advanceTurn();
        advanceTurn();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(island);
    }

    @Test
    @DisplayName("A land of another type is unaffected")
    void otherLandTypeUnaffected() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        shimmerWithType(CardSubtype.ISLAND);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Grants phasing regardless of who controls the land")
    void appliesToOpponentLands() {
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        shimmerWithType(CardSubtype.ISLAND);

        harness.forceActivePlayer(player2);
        advanceTurn(); // player1's untap step
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentIsland);

        advanceTurn(); // player2's untap step

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentIsland);
    }

    @Test
    @DisplayName("Lands stop phasing once Shimmer leaves the battlefield")
    void phasingEndsWhenShimmerLeaves() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent shimmer = shimmerWithType(CardSubtype.ISLAND);
        gd.playerBattlefields.get(player1.getId()).remove(shimmer);

        harness.forceActivePlayer(player1);
        advanceTurn();
        advanceTurn();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island);
    }
}
