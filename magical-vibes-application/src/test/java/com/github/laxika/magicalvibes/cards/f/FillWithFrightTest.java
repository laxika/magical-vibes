package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FillWithFright.class, Arachnoid.class})
class FillWithFrightTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards, then the caster scries two")
    void discardsTwoThenScriesTwo() {
        Arachnoid firstDiscard = new Arachnoid();
        Arachnoid secondDiscard = new Arachnoid();
        harness.setHand(player2, List.of(firstDiscard, secondDiscard));

        Arachnoid topCard = new Arachnoid();
        Arachnoid secondCard = new Arachnoid();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.setHand(player1, List.of(new FillWithFright()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstDiscard, secondDiscard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        harness.assertInGraveyard(player1, "Fill with Fright");
    }

    @Test
    @DisplayName("The caster may be targeted and discards the one card available")
    void canTargetCasterWithFewerThanTwoCards() {
        Arachnoid onlyDiscard = new Arachnoid();
        harness.setHand(player1, List.of(new FillWithFright(), onlyDiscard));
        harness.setLibrary(player1, List.of(new Arachnoid(), new Arachnoid()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.playerId()).isEqualTo(player1.getId());
        assertThat(discardChoice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyDiscard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);
    }

    @Test
    @DisplayName("An empty target hand still allows the caster to scry two")
    void emptyTargetHandStillScries() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new FillWithFright()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new Arachnoid());
        harness.setHand(player1, List.of(new FillWithFright()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0,
                harness.getPermanentId(player2, "Arachnoid")))
                .isInstanceOf(IllegalStateException.class);
    }
}
