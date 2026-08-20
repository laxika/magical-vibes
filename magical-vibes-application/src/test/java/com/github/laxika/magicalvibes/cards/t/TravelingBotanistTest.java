package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TravelingBotanistTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped lets you reveal a land into your hand")
    void becomingTappedCanPutLandIntoHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        tapBotanist();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Declining the land reveal may put it into your graveyard")
    void decliningLandRevealCanPutLandIntoGraveyard() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        tapBotanist();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("A nonland top card may be put into your graveyard")
    void nonlandCanBePutIntoGraveyard() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        tapBotanist();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    @DisplayName("Declining both choices leaves the top card on the library")
    void decliningBothChoicesLeavesCardOnTop() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        tapBotanist();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Tapping another permanent does not trigger Traveling Botanist")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new TravelingBotanist());
        harness.addToBattlefield(player1, new Forest());
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        harness.tapPermanent(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void tapBotanist() {
        harness.addToBattlefield(player1, new TravelingBotanist());
        harness.tapPermanent(player1, 0);
        harness.passBothPriorities();
    }
}
