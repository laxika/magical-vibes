package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TravelingBotanist.class, Forest.class})
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
        Card nonland = new TravelingBotanist();
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
        Permanent botanist = harness.addToBattlefieldAndReturn(player1, new TravelingBotanist());
        botanist.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, botanist));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining to mill a nonland leaves it on top")
    void decliningNonlandLeavesItOnTop() {
        Card nonland = new TravelingBotanist();
        harness.setLibrary(player1, List.of(nonland));

        tapBotanist();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    @DisplayName("An empty library produces no optional choice")
    void emptyLibraryProducesNoChoice() {
        harness.setLibrary(player1, List.of());

        tapBotanist();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking triggers the ability through the combat tap path")
    void attackingTriggersAbility() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        addCreatureReady(player1, new TravelingBotanist());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("The triggered ability resolves after its source leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        Permanent botanist = harness.addToBattlefieldAndReturn(player1, new TravelingBotanist());
        botanist.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, botanist));
        gd.playerBattlefields.get(player1.getId()).remove(botanist);
        gd.playerGraveyards.get(player1.getId()).add(botanist.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Only the Botanist that becomes tapped triggers")
    void anotherBotanistDoesNotTrigger() {
        Card land = new Forest();
        Card nextCard = new TravelingBotanist();
        harness.setLibrary(player1, List.of(land, nextCard));
        harness.addToBattlefield(player1, new TravelingBotanist());

        tapBotanist();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The top card is determined when the triggered ability resolves")
    void looksAtTopCardAtResolution() {
        Card originalTop = new TravelingBotanist();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(originalTop));
        Permanent botanist = harness.addToBattlefieldAndReturn(player1, new TravelingBotanist());
        botanist.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, botanist));
        harness.setLibrary(player1, List.of(land, originalTop));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
    }
}
