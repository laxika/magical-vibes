package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.cards.s.StompAndHowl;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrainPry.class, BloodCrypt.class, CoilingOracle.class, StompAndHowl.class})
class BrainPryTest extends BaseCardTest {

    @Test
    @DisplayName("The target chooses one matching card to discard")
    void targetChoosesOneMatchingCard() {
        Card firstStompAndHowl = new StompAndHowl();
        Card secondStompAndHowl = new StompAndHowl();
        Card coilingOracle = new CoilingOracle();
        cast(List.of(firstStompAndHowl, secondStompAndHowl, coilingOracle));

        harness.handleListChoice(player1, "Stomp and Howl");

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstStompAndHowl, coilingOracle);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondStompAndHowl);
    }

    @Test
    @DisplayName("Draws a card when the named card is absent from the target's hand")
    void drawsWhenNamedCardIsAbsent() {
        CoilingOracle drawnCard = new CoilingOracle();
        harness.setLibrary(player1, List.of(drawnCard));
        cast(List.of(new StompAndHowl()));

        harness.handleListChoice(player1, "Coiling Oracle");

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Stomp and Howl");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Only offers nonland card names")
    void onlyOffersNonlandCardNames() {
        cast(List.of(new BloodCrypt(), new StompAndHowl()));

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Stomp and Howl").doesNotContain("Blood Crypt");
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        BrainPry brainPry = new BrainPry();
        Card firstStompAndHowl = new StompAndHowl();
        Card secondStompAndHowl = new StompAndHowl();
        harness.setHand(player1, List.of(brainPry, firstStompAndHowl, secondStompAndHowl));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.handleListChoice(player1, "Stomp and Howl");
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstStompAndHowl);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(brainPry, secondStompAndHowl);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void rejectsPermanentTarget() {
        BrainPry brainPry = new BrainPry();
        harness.setHand(player1, List.of(brainPry));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoilingOracle());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(brainPry);
    }

    private void cast(List<Card> targetHand) {
        harness.setHand(player1, List.of(new BrainPry()));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
