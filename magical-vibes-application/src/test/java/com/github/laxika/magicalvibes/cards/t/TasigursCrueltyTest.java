package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BattleBrawler;
import com.github.laxika.magicalvibes.cards.m.MarduShadowspear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TasigursCruelty.class, BattleBrawler.class, MarduShadowspear.class})
class TasigursCrueltyTest extends BaseCardTest {

    @Test
    void eachOpponentDiscardsTwoCards() {
        harness.setHand(player2, List.of(new BattleBrawler(), new MarduShadowspear(), new TasigursCruelty()));
        castTasigursCruelty();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void delveExilesGraveyardCardsToPayGenericMana() {
        List<Card> graveyard = List.of(new MarduShadowspear(), new MarduShadowspear(),
                new MarduShadowspear(), new MarduShadowspear(), new MarduShadowspear());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TasigursCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        castWithDelve(List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
    }

    @Test
    void opponentWithOneCardDiscardsOnlyThatCard() {
        Card card = new BattleBrawler();
        harness.setHand(player2, List.of(card));
        castTasigursCruelty();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyOpponentHandDoesNotPreventResolution() {
        harness.setHand(player2, List.of());
        castTasigursCruelty();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void controllerKeepsOtherCardsInHand() {
        Card retained = new BattleBrawler();
        harness.setHand(player1, List.of(new TasigursCruelty(), retained));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void partialDelvePaysRemainingGenericManaAndLeavesUnselectedCards() {
        Card first = new BattleBrawler();
        Card second = new MarduShadowspear();
        Card retained = new TasigursCruelty();
        harness.setGraveyard(player1, List.of(first, second, retained));
        harness.setHand(player1, List.of(new TasigursCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castWithDelve(List.of(0, 1));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void delveCannotPayTheBlackManaRequirement() {
        harness.setGraveyard(player1, List.of(new BattleBrawler(), new BattleBrawler(),
                new BattleBrawler(), new BattleBrawler(), new BattleBrawler()));
        harness.setHand(player1, List.of(new TasigursCruelty()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> castWithDelve(List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    private void castWithDelve(List<Integer> indices) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, indices);
    }

    private void castTasigursCruelty() {
        harness.setHand(player1, List.of(new TasigursCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
