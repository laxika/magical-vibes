package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KolaghanAspirant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SibsigIcebreakers.class, SummitProwler.class, KolaghanAspirant.class})
class SibsigIcebreakersTest extends BaseCardTest {

    private void castSibsigIcebreakers() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB makes each player choose a card to discard in APNAP order")
    void eachPlayerDiscards() {
        harness.setHand(player1, List.of(new SibsigIcebreakers(), new SummitProwler()));
        harness.setHand(player2, List.of(new KolaghanAspirant()));

        castSibsigIcebreakers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Summit Prowler");
        harness.assertInGraveyard(player2, "Kolaghan Aspirant");
    }

    @Test
    @DisplayName("A player with an empty hand discards nothing")
    void emptyHandDiscardsNothing() {
        harness.setHand(player2, List.of(new KolaghanAspirant()));

        harness.castFromHand(player1, new SibsigIcebreakers(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discard choices stay hidden until every player has chosen")
    void discardsHappenSimultaneouslyAfterAllChoices() {
        harness.setHand(player1, List.of(new SibsigIcebreakers(), new SummitProwler(), new KolaghanAspirant()));
        harness.setHand(player2, List.of(new SummitProwler(), new KolaghanAspirant()));

        castSibsigIcebreakers();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertInHand(player1, "Summit Prowler");
        harness.assertNotInGraveyard(player1, "Summit Prowler");
        harness.assertInHand(player2, "Kolaghan Aspirant");

        harness.handleCardChosen(player2, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Summit Prowler");
        harness.assertInGraveyard(player2, "Kolaghan Aspirant");
        harness.assertInHand(player1, "Kolaghan Aspirant");
        harness.assertInHand(player2, "Summit Prowler");
    }

    @Test
    @DisplayName("Each player chooses exactly one card from a larger hand")
    void eachPlayerChoosesOneCard() {
        harness.setHand(player1, List.of(new SibsigIcebreakers(), new SummitProwler(), new KolaghanAspirant()));
        harness.setHand(player2, List.of(new SummitProwler(), new KolaghanAspirant()));

        castSibsigIcebreakers();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Kolaghan Aspirant");
        harness.assertInGraveyard(player2, "Summit Prowler");
        harness.assertInHand(player1, "Summit Prowler");
        harness.assertInHand(player2, "Kolaghan Aspirant");
    }

    @Test
    @DisplayName("An empty opposing hand does not prevent the controller from discarding")
    void opponentWithEmptyHandIsSkipped() {
        harness.setHand(player1, List.of(new SibsigIcebreakers(), new SummitProwler()));
        harness.setHand(player2, List.of());

        castSibsigIcebreakers();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Summit Prowler");
        harness.assertOnBattlefield(player1, "Sibsig Icebreakers");
    }

    @Test
    @DisplayName("The trigger completes without a choice when both hands are empty")
    void bothHandsEmpty() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new SibsigIcebreakers(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Sibsig Icebreakers");
        harness.assertNotInGraveyard(player1, "Sibsig Icebreakers");
    }
}
