package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.ForebodingFruit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GarrukCursedHuntsman;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectersShriek.class, ForebodingFruit.class, Forest.class, Gingerbrute.class, GarrukCursedHuntsman.class})
class SpectersShriekTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a nonblack card also exiles a card from the caster's hand")
    void nonblackCardCausesCasterToExileFromHand() {
        Card casterCard = new Forest();
        Card opponentCard = new Gingerbrute();
        harness.setHand(player1, List.of(new SpectersShriek(), casterCard));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SpectersShriekChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(casterCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiling a black card does not exile a card from the caster's hand")
    void blackCardDoesNotCauseCasterToExileFromHand() {
        Card casterCard = new Forest();
        Card opponentCard = new ForebodingFruit();
        harness.setHand(player1, List.of(new SpectersShriek(), casterCard));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional card choice does nothing")
    void mayDeclineCardChoice() {
        Card opponentCard = new Gingerbrute();
        harness.setHand(player1, List.of(new SpectersShriek()));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SpectersShriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land cannot be chosen from a mixed hand")
    void cannotChooseLand() {
        Card land = new Forest();
        Card nonland = new Gingerbrute();
        harness.setHand(player1, List.of(new SpectersShriek()));
        harness.setHand(player2, List.of(land, nonland));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonland);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A hand containing only lands leaves both hands unchanged")
    void onlyLandsCannotBeExiled() {
        Card casterCard = new ForebodingFruit();
        Card land = new Forest();
        harness.setHand(player1, List.of(new SpectersShriek(), casterCard));
        harness.setHand(player2, List.of(land));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty opponent hand causes no exile")
    void emptyOpponentHand() {
        Card casterCard = new Forest();
        harness.setHand(player1, List.of(new SpectersShriek(), casterCard));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A multicolored black card does not require the caster to exile a card")
    void multicoloredBlackCardDoesNotCauseSelfExile() {
        Card casterCard = new Forest();
        Card opponentCard = new GarrukCursedHuntsman();
        harness.setHand(player1, List.of(new SpectersShriek(), casterCard));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The caster chooses exactly one card to exile and cannot decline")
    void casterExileIsMandatoryAndOnlyOneCard() {
        Card first = new Forest();
        Card second = new ForebodingFruit();
        Card opponentCard = new Gingerbrute();
        harness.setHand(player1, List.of(new SpectersShriek(), first, second));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
