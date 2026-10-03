package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisinformationCampaign.class, DazzlingLights.class, BartizanBats.class})
class DisinformationCampaignTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, you draw a card and each opponent discards a card")
    void entersDrawsAndEachOpponentDiscards() {
        Card drawn = new BartizanBats();
        Card discarded = new BartizanBats();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(discarded));
        harness.castFromHand(player1, new DisinformationCampaign(), "{1}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Bartizan Bats");
    }

    @Test
    @DisplayName("Whenever you surveil, it returns to its owner's hand")
    void surveilingReturnsItToHand() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new DisinformationCampaign());
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(campaign);
        assertThat(gd.playerHands.get(player1.getId())).contains(campaign.getCard());
    }

    @Test
    @DisplayName("An opponent with no cards does not prevent the controller from drawing")
    void drawsWithEmptyOpponentHand() {
        Card drawn = new BartizanBats();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new DisinformationCampaign(), "{1}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Disinformation Campaign");
    }

    @Test
    @DisplayName("Keeping all surveilled cards still returns the enchantment")
    void keepingAllSurveilledCardsReturnsCampaign() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new DisinformationCampaign());
        Card first = new BartizanBats();
        Card second = new BartizanBats();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(campaign);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(campaign.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Disinformation Campaign");
    }

    @Test
    @DisplayName("Surveilling an empty library still returns the enchantment")
    void surveilingEmptyLibraryReturnsCampaign() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new DisinformationCampaign());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(campaign.getCard());
        harness.assertNotOnBattlefield(player1, "Disinformation Campaign");
    }

    @Test
    @DisplayName("An opponent surveilling does not return the enchantment")
    void opponentSurveillingDoesNotReturnCampaign() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new DisinformationCampaign());
        harness.setLibrary(player2, List.of(new BartizanBats(), new BartizanBats()));
        harness.setHand(player2, List.of(new DazzlingLights()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BartizanBats());

        harness.castAndResolveInstant(player2, 0, target.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(campaign);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(campaign.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A campaign controlled by another player returns to its owner")
    void returnsToOwnerRatherThanController() {
        Card campaignCard = new DisinformationCampaign();
        campaignCard.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, campaignCard);
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(campaignCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(campaignCard);
        harness.assertNotOnBattlefield(player1, "Disinformation Campaign");
    }
}
